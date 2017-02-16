/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.registry.consul;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.NotifyListener.NotifyRouterListener;
import com.quancheng.saluki.core.registry.internal.FailbackRegistry;
import com.quancheng.saluki.core.utils.CollectionUtils;
import com.quancheng.saluki.registry.consul.internal.ConsulClient;
import com.quancheng.saluki.registry.consul.model.ConsulEphemralNode;
import com.quancheng.saluki.registry.consul.model.ConsulRouterResp;
import com.quancheng.saluki.registry.consul.model.ConsulService;
import com.quancheng.saluki.registry.consul.model.ConsulServiceResp;
import com.quancheng.saluki.registry.consul.model.ThrallRoleType;

/**
 * @author shimingliu 2016年12月16日 上午10:24:02
 * @version ConsulRegistry.java, v 0.0.1 2016年12月16日 上午10:24:02 shimingliu
 */
public class ConsulRegistry extends FailbackRegistry {

    private static final Logger                                                         log                    = LoggerFactory.getLogger(ConsulRegistry.class);
    private final ConsulClient                                                          client;
    private final Cache<String, Map<String, List<GrpcURL>>>                             serviceCache;
    private final Map<String, Long>                                                     lookupGroupServices    = Maps.newConcurrentMap();
    private final Map<String, Pair<GrpcURL, Set<NotifyListener.NotifyServiceListener>>> notifyServiceListeners = Maps.newConcurrentMap();
    private final Set<String>                                                           serviceGroupLookUped   = Sets.newConcurrentHashSet();

    public ConsulRegistry(GrpcURL url){
        super(url);
        String host = url.getHost();
        int port = url.getPort();
        client = new ConsulClient(host, port);
        this.serviceCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    }

    private ConsulService buildConsulHealthService(GrpcURL url) {
        return ConsulService.newSalukiService()//
                            .withAddress(url.getHost())//
                            .withPort(Integer.valueOf(url.getPort()).toString())//
                            .withName(GrpcURLUtils.toServiceName(url.getGroup()))//
                            .withTag(GrpcURLUtils.healthServicePath(url, ThrallRoleType.PROVIDER))//
                            .withId(url.getHost() + ":" + url.getPort() + "-" + url.getPath() + "-" + url.getVersion())//
                            .withCheckInterval(Integer.valueOf(ConsulConstants.TTL).toString()).build();
    }

    private ConsulEphemralNode buildEphemralNode(GrpcURL url, ThrallRoleType roleType) {
        return ConsulEphemralNode.newEphemralNode().withUrl(url)//
                                 .withEphemralType(roleType)//
                                 .withCheckInterval(Integer.toString(ConsulConstants.TTL * 6))//
                                 .build();
    }

    @Override
    protected void doRegister(GrpcURL url) {
        ConsulService service = this.buildConsulHealthService(url);
        client.registerService(service);
        ConsulEphemralNode ephemralNode = this.buildEphemralNode(url, ThrallRoleType.PROVIDER);
        client.registerEphemralNode(ephemralNode);
    }

    @Override
    protected void doUnregister(GrpcURL url) {
        ConsulService service = this.buildConsulHealthService(url);
        client.unregisterService(service);
    }

    @Override
    protected synchronized void doSubscribe(GrpcURL url, NotifyListener.NotifyServiceListener listener) {
        Pair<GrpcURL, Set<NotifyListener.NotifyServiceListener>> listenersPair = notifyServiceListeners.get(url.getServiceKey());
        if (listenersPair == null) {
            Set<NotifyListener.NotifyServiceListener> listeners = Sets.newConcurrentHashSet();
            listeners.add(listener);
            listenersPair = new ImmutablePair<GrpcURL, Set<NotifyListener.NotifyServiceListener>>(url, listeners);
        } else {
            listenersPair.getValue().add(listener);
        }
        notifyServiceListeners.putIfAbsent(url.getServiceKey(), listenersPair);
        if (!serviceGroupLookUped.contains(url.getGroup())) {
            serviceGroupLookUped.add(url.getGroup());
            ServiceLookUper serviceLookUper = new ServiceLookUper(url.getGroup());
            serviceLookUper.setDaemon(true);
            serviceLookUper.start();
            ConsulEphemralNode ephemralNode = this.buildEphemralNode(url, ThrallRoleType.CONSUMER);
            client.registerEphemralNode(ephemralNode);
        } else {
            notifyListener(url, listener);
        }
    }

    @Override
    protected void doUnsubscribe(GrpcURL url, NotifyListener.NotifyServiceListener listener) {
        notifyServiceListeners.remove(url.getServiceKey());
    }

    @Override
    public List<GrpcURL> discover(GrpcURL url) {
        String group = url.getGroup();
        try {
            Map<String, List<GrpcURL>> providerUrls = serviceCache.get(group,
                                                                       new Callable<Map<String, List<GrpcURL>>>() {

                                                                           @Override
                                                                           public Map<String, List<GrpcURL>> call() throws Exception {
                                                                               return lookupServiceUpdate(group);
                                                                           }
                                                                       });
            return providerUrls.get(url.getServiceKey());
        } catch (ExecutionException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private void notifyListener(GrpcURL url, NotifyListener.NotifyServiceListener listener) {
        Map<String, List<GrpcURL>> groupCacheUrls = serviceCache.getIfPresent(url.getGroup());
        if (groupCacheUrls != null) {
            for (Map.Entry<String, List<GrpcURL>> entry : groupCacheUrls.entrySet()) {
                String cacheServiceKey = entry.getKey();
                if (url.getServiceKey().equals(cacheServiceKey)) {
                    List<GrpcURL> newUrls = entry.getValue();
                    ConsulRegistry.this.notify(url, listener, newUrls);
                }
            }
        }
    }

    private Map<String, List<GrpcURL>> lookupServiceUpdate(String group) {
        Long lastConsulIndexId = lookupGroupServices.get(group) == null ? 0L : lookupGroupServices.get(group);
        String serviceName = GrpcURLUtils.toServiceName(group);
        ConsulServiceResp consulResp = client.lookupHealthService(serviceName, lastConsulIndexId);
        if (consulResp != null) {
            List<ConsulService> consulServcies = consulResp.getSalukiConsulServices();
            boolean updated = consulServcies != null && !consulServcies.isEmpty()
                              && consulResp.getConsulIndex() > lastConsulIndexId;
            if (updated) {
                Map<String, List<GrpcURL>> groupProviderUrls = Maps.newConcurrentMap();
                for (ConsulService service : consulServcies) {
                    GrpcURL providerUrl = buildURL(service);
                    String serviceKey = providerUrl.getServiceKey();
                    List<GrpcURL> urlList = groupProviderUrls.get(serviceKey);
                    if (urlList == null) {
                        urlList = Lists.newArrayList();
                        groupProviderUrls.put(serviceKey, urlList);
                    }
                    urlList.add(providerUrl);
                }
                lookupGroupServices.put(group, consulResp.getConsulIndex());
                return groupProviderUrls;
            }
        }
        return null;
    }

    private GrpcURL buildURL(ConsulService service) {
        try {
            for (String tag : service.getTags()) {
                if (StringUtils.indexOf(tag, Constants.PROVIDERS_CATEGORY) != -1) {
                    String toUrlPath = StringUtils.substringAfter(tag, Constants.PROVIDERS_CATEGORY);
                    GrpcURL salukiUrl = GrpcURL.valueOf(GrpcURL.decode(toUrlPath));
                    return salukiUrl;
                }
            }
        } catch (Exception e) {
            log.error("convert consul service to url fail! service:" + service, e);
        }
        return null;
    }

    private class ServiceLookUper extends Thread {

        private final String group;

        public ServiceLookUper(String group){
            this.group = group;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // 最新拉取的值
                    Map<String, List<GrpcURL>> groupNewUrls = lookupServiceUpdate(group);
                    if (groupNewUrls != null && !groupNewUrls.isEmpty()) {
                        // 缓存中的值
                        Map<String, List<GrpcURL>> groupCacheUrls = serviceCache.getIfPresent(group);
                        if (groupCacheUrls == null) {
                            groupCacheUrls = Maps.newConcurrentMap();
                            serviceCache.put(group, groupCacheUrls);
                        }
                        for (Map.Entry<String, List<GrpcURL>> entry : groupNewUrls.entrySet()) {
                            List<GrpcURL> oldUrls = groupCacheUrls.get(entry.getKey());
                            List<GrpcURL> newUrls = entry.getValue();
                            boolean isSame = CollectionUtils.isSameCollection(newUrls, oldUrls);
                            if (!isSame) {
                                groupCacheUrls.put(entry.getKey(), newUrls);
                                Pair<GrpcURL, Set<NotifyListener.NotifyServiceListener>> listenerPair = notifyServiceListeners.get(entry.getKey());
                                if (listenerPair != null) {
                                    GrpcURL subscribeUrl = listenerPair.getKey();
                                    Set<NotifyListener.NotifyServiceListener> listeners = listenerPair.getValue();
                                    for (NotifyListener.NotifyServiceListener listener : listeners) {
                                        ConsulRegistry.this.notify(subscribeUrl, listener, newUrls);
                                    }
                                }
                            }
                        }
                    }
                    sleep(ConsulConstants.DEFAULT_LOOKUP_INTERVAL);
                } catch (Throwable e) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    /**
     * Router信息
     */
    private final Map<String, Long>                                     lookupGroupRouters    = Maps.newConcurrentMap();
    private final Map<String, Set<NotifyListener.NotifyRouterListener>> notifyRouterListeners = Maps.newConcurrentMap();
    private final Set<String>                                           routerGroupLookUped   = Sets.newConcurrentHashSet();

    @Override
    public void subscribe(String group, NotifyRouterListener listener) {
        Set<NotifyListener.NotifyRouterListener> listeners = notifyRouterListeners.get(group);
        if (listeners == null) {
            listeners = Sets.newConcurrentHashSet();
            listeners.add(listener);
        } else {
            listeners.add(listener);
        }
        notifyRouterListeners.put(group, listeners);
        if (!routerGroupLookUped.contains(group)) {
            routerGroupLookUped.add(group);
            RouterLookUper routerLookUper = new RouterLookUper(group);
            routerLookUper.setDaemon(true);
            routerLookUper.start();
        }
    }

    @Override
    public void unsubscribe(String group, NotifyRouterListener listener) {
        notifyRouterListeners.get(group).remove(listener);
    }

    private class RouterLookUper extends Thread {

        private final String          group;
        private final ExecutorService notifyExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()
                                                                                    * 3,
                                                                                    new NamedThreadFactory("SalukiNotifyListener.NotifyServiceListener",
                                                                                                           true));

        public RouterLookUper(String group){
            this.group = group;
        }

        private String lookupRouterUpdate(String group) {
            Long lastConsulIndexId = lookupGroupRouters.get(group) == null ? 0L : lookupGroupRouters.get(group);
            String serviceName = GrpcURLUtils.toServiceName(group);
            ConsulRouterResp consulResp = client.lookupRouterMessage(serviceName, lastConsulIndexId);
            if (consulResp != null) {
                String routerMessages = consulResp.getSalukiConsulRouter();
                lookupGroupRouters.put(group, consulResp.getConsulIndex());
                return routerMessages;
            }
            return null;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String groupRouterMessages = lookupRouterUpdate(group);
                    Set<NotifyListener.NotifyRouterListener> listeners = notifyRouterListeners.get(group);
                    if (listeners != null) {
                        for (NotifyListener.NotifyRouterListener listener : listeners) {
                            notifyExecutor.submit(new Runnable() {

                                @Override
                                public void run() {
                                    listener.notify(group, groupRouterMessages);
                                }
                            });
                        }
                    }
                    sleep(ConsulConstants.DEFAULT_LOOKUP_INTERVAL);
                } catch (Throwable e) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }
}
