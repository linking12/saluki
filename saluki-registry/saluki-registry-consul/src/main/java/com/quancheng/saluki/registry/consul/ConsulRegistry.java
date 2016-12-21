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
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.core.common.ThrallURL;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.support.FailbackRegistry;
import com.quancheng.saluki.registry.consul.internal.ConsulClient;
import com.quancheng.saluki.registry.consul.model.ConsulEphemralNode;
import com.quancheng.saluki.registry.consul.model.ConsulService;
import com.quancheng.saluki.registry.consul.model.ConsulServiceResp;
import com.quancheng.saluki.registry.consul.model.ThrallRoleType;

/**
 * @author shimingliu 2016年12月16日 上午10:24:02
 * @version ConsulRegistry.java, v 0.0.1 2016年12月16日 上午10:24:02 shimingliu
 */
public class ConsulRegistry extends FailbackRegistry {

    private static final Logger                                     log = LoggerFactory.getLogger(ConsulRegistry.class);

    private final ConsulClient                                      client;

    private final Cache<String, Map<String, List<ThrallURL>>>       serviceCache;

    private final Map<String, Long>                                 lookupGroupServices;

    private final ExecutorService                                   lookUpServiceExecutor;

    private final Map<String, Pair<ThrallURL, Set<NotifyListener>>> notifyListeners;

    private final Set<String>                                       groupLoogUped;

    public ConsulRegistry(ThrallURL url){
        super(url);
        String host = url.getHost();
        int port = url.getPort();
        client = new ConsulClient(host, port);
        notifyListeners = Maps.newConcurrentMap();
        serviceCache = CacheBuilder.newBuilder().maximumSize(1000).build();
        lookupGroupServices = Maps.newConcurrentMap();
        groupLoogUped = Sets.newConcurrentHashSet();
        this.lookUpServiceExecutor = Executors.newFixedThreadPool(1,
                                                                  new NamedThreadFactory("ConsulLookUpService", true));
    }

    private ConsulService buildConsulHealthService(ThrallURL url) {
        return ConsulService.newSalukiService()//
                            .withAddress(url.getHost())//
                            .withPort(Integer.valueOf(url.getPort()).toString())//
                            .withName(ThrallURLUtils.toServiceName(url.getGroup()))//
                            .withTag(ThrallURLUtils.healthServicePath(url, ThrallRoleType.PROVIDER))//
                            .withId(url.getHost() + ":" + url.getPort() + "-" + url.getPath() + "-" + url.getVersion())//
                            .withCheckInterval(Integer.valueOf(ConsulConstants.TTL).toString()).build();
    }

    private ConsulEphemralNode buildEphemralNode(ThrallURL url, ThrallRoleType roleType) {
        return ConsulEphemralNode.newEphemralNode().withUrl(url)//
                                 .withEphemralType(roleType)//
                                 .withCheckInterval(Integer.toString(ConsulConstants.TTL * 600))//
                                 .build();

    }

    @Override
    protected void doRegister(ThrallURL url) {
        ConsulService service = this.buildConsulHealthService(url);
        client.registerService(service);
        ConsulEphemralNode ephemralNode = this.buildEphemralNode(url, ThrallRoleType.PROVIDER);
        client.registerEphemralNode(ephemralNode);
    }

    @Override
    protected void doUnregister(ThrallURL url) {
        ConsulService service = this.buildConsulHealthService(url);
        client.unregisterService(service.getId());
    }

    @Override
    protected synchronized void doSubscribe(ThrallURL url, NotifyListener listener) {
        Pair<ThrallURL, Set<NotifyListener>> listenersPair = notifyListeners.get(url.getServiceKey());
        if (listenersPair == null) {
            Set<NotifyListener> listeners = Sets.newConcurrentHashSet();
            listeners.add(listener);
            listenersPair = new ImmutablePair<ThrallURL, Set<NotifyListener>>(url, listeners);
        } else {
            listenersPair.getValue().add(listener);
        }
        notifyListeners.putIfAbsent(url.getServiceKey(), listenersPair);
        if (!groupLoogUped.contains(url.getGroup())) {
            groupLoogUped.add(url.getGroup());
            lookUpServiceExecutor.execute(new ServiceLookUper(url.getGroup()));
            ConsulEphemralNode ephemralNode = this.buildEphemralNode(url, ThrallRoleType.CONSUMER);
            client.registerEphemralNode(ephemralNode);
        } else {
            notifyListener(url, listener);
        }
    }

    private void notifyListener(ThrallURL url, NotifyListener listener) {
        Map<String, List<ThrallURL>> groupCacheUrls = serviceCache.getIfPresent(url.getGroup());
        if (groupCacheUrls != null) {
            for (Map.Entry<String, List<ThrallURL>> entry : groupCacheUrls.entrySet()) {
                String cacheServiceKey = entry.getKey();
                if (url.getServiceKey().equals(cacheServiceKey)) {
                    List<ThrallURL> newUrls = entry.getValue();
                    ConsulRegistry.this.notify(url, listener, newUrls);
                }
            }
        }
    }

    @Override
    protected void doUnsubscribe(ThrallURL url, NotifyListener listener) {
        notifyListeners.remove(url.getServiceKey());
    }

    @Override
    public List<ThrallURL> discover(ThrallURL url) {
        String group = url.getGroup();
        return lookupServiceUpdate(group).get(url.getServiceKey());
    }

    private Map<String, List<ThrallURL>> lookupServiceUpdate(String group) {
        Long lastConsulIndexId = lookupGroupServices.get(group) == null ? 0L : lookupGroupServices.get(group);
        String serviceName = ThrallURLUtils.toServiceName(group);
        ConsulServiceResp consulResp = client.lookupHealthService(serviceName, lastConsulIndexId);
        if (consulResp != null) {
            List<ConsulService> consulServcies = consulResp.getSalukiConsulServices();
            boolean updated = consulServcies != null && !consulServcies.isEmpty()
                              && consulResp.getConsulIndex() > lastConsulIndexId;
            if (updated) {
                Map<String, List<ThrallURL>> groupProviderUrls = Maps.newConcurrentMap();
                for (ConsulService service : consulServcies) {
                    ThrallURL providerUrl = buildURL(service);
                    String serviceKey = providerUrl.getServiceKey();
                    List<ThrallURL> urlList = groupProviderUrls.get(serviceKey);
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

    private ThrallURL buildURL(ConsulService service) {
        try {
            for (String tag : service.getTags()) {
                if (StringUtils.indexOf(tag, Constants.PROVIDERS_CATEGORY) != -1) {
                    String toUrlPath = StringUtils.substringAfter(tag, Constants.PROVIDERS_CATEGORY);
                    ThrallURL salukiUrl = ThrallURL.valueOf(ThrallURL.decode(toUrlPath));
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

        private boolean haveChanged(List<ThrallURL> newUrls, List<ThrallURL> oldUrls) {
            if (newUrls == null | newUrls.isEmpty()) {
                return false;
            } else if (oldUrls != null && oldUrls.containsAll(newUrls)) {
                return false;
            }
            return true;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // 最新拉取的值
                    Map<String, List<ThrallURL>> groupNewUrls = lookupServiceUpdate(group);
                    if (groupNewUrls != null && !groupNewUrls.isEmpty()) {
                        // 缓存中的值
                        Map<String, List<ThrallURL>> groupCacheUrls = serviceCache.getIfPresent(group);
                        if (groupCacheUrls == null) {
                            groupCacheUrls = Maps.newConcurrentMap();
                            serviceCache.put(group, groupCacheUrls);
                        }
                        for (Map.Entry<String, List<ThrallURL>> entry : groupNewUrls.entrySet()) {
                            List<ThrallURL> oldUrls = groupCacheUrls.get(entry.getKey());
                            List<ThrallURL> newUrls = entry.getValue();
                            boolean haveChanged = haveChanged(newUrls, oldUrls);
                            if (haveChanged) {
                                groupCacheUrls.put(entry.getKey(), newUrls);
                                Pair<ThrallURL, Set<NotifyListener>> listenerPair = notifyListeners.get(entry.getKey());
                                if (listenerPair != null) {
                                    ThrallURL subscribeUrl = listenerPair.getKey();
                                    Set<NotifyListener> listeners = listenerPair.getValue();
                                    for (NotifyListener listener : listeners) {
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

}
