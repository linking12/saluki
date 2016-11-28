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
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.support.FailbackRegistry;
import com.quancheng.saluki.core.utils.NamedThreadFactory;
import com.quancheng.saluki.registry.consul.internal.ConsulConstants;
import com.quancheng.saluki.registry.consul.internal.SalukiConsulClient;
import com.quancheng.saluki.registry.consul.internal.model.SalukiConsulEphemralNode;
import com.quancheng.saluki.registry.consul.internal.model.SalukiConsulService;
import com.quancheng.saluki.registry.consul.internal.model.SalukiConsulServiceResp;

public class ConsulRegistry extends FailbackRegistry {

    private static final Logger                                     log                = LoggerFactory.getLogger(ConsulRegistry.class);
    public static final String                                      CONSUL_SERVICE_PRE = "Saluki_";
    private final SalukiConsulClient                                client;
    private final Cache<String, Map<String, List<SalukiURL>>>       serviceCache;
    private final Map<String, Long>                                 lookupGroupServices;
    private final ExecutorService                                   lookUpServiceExecutor;
    private final Map<String, Pair<SalukiURL, Set<NotifyListener>>> notifyListeners;
    private final Set<String>                                       groupLoogUped;

    public ConsulRegistry(SalukiURL url){
        super(url);
        String host = url.getHost();
        int port = url.getPort();
        client = new SalukiConsulClient(host, port);
        notifyListeners = Maps.newConcurrentMap();
        serviceCache = CacheBuilder.newBuilder().maximumSize(1000).build();
        lookupGroupServices = Maps.newConcurrentMap();
        groupLoogUped = Sets.newConcurrentHashSet();
        int cpus = super.getCpus();
        this.lookUpServiceExecutor = Executors.newFixedThreadPool(cpus * 10,
                                                                  new NamedThreadFactory("ConsulLookUpService", true));
    }

    @Override
    protected void doRegister(SalukiURL url) {
        SalukiConsulService service = this.buildService(url);
        client.registerService(service);
        // 注册本机地址到consul中
        SalukiConsulEphemralNode ephemralNode = this.buildEphemralNode(url, "provider");
        client.registerEphemralNode(ephemralNode);
    }

    @Override
    protected void doUnregister(SalukiURL url) {
        SalukiConsulService service = this.buildService(url);
        client.unregisterService(service.getId());
    }

    @Override
    protected synchronized void doSubscribe(SalukiURL url, NotifyListener listener) {
        Pair<SalukiURL, Set<NotifyListener>> listenersPair = notifyListeners.get(url.getServiceKey());
        if (listenersPair == null) {
            Set<NotifyListener> listeners = Sets.newConcurrentHashSet();
            listeners.add(listener);
            listenersPair = new ImmutablePair<SalukiURL, Set<NotifyListener>>(url, listeners);
        } else {
            listenersPair.getValue().add(listener);
        }
        notifyListeners.putIfAbsent(url.getServiceKey(), listenersPair);
        if (!groupLoogUped.contains(url.getGroup())) {
            groupLoogUped.add(url.getGroup());
            lookUpServiceExecutor.execute(new ServiceLookUper(url.getGroup()));
            // 注册本机地址到consul中
            SalukiConsulEphemralNode ephemralNode = this.buildEphemralNode(url, "consumer");
            client.registerEphemralNode(ephemralNode);
        }
        // 如果缓存中有，先把缓存中的数据吐出去
        notifyListener(url, listener);
    }

    @Override
    protected void doUnsubscribe(SalukiURL url, NotifyListener listener) {
        notifyListeners.remove(url.getServiceKey());
    }

    @Override
    public List<SalukiURL> discover(SalukiURL url) {
        String[] keys = new String[] { SalukiConstants.GRPC_IN_LOCAL_PROCESS, SalukiConstants.RPCTYPE_KEY,
                                       SalukiConstants.GENERIC_KEY, SalukiConstants.RPCTIMEOUT_KEY };
        url = url.removeParameters(keys);
        String group = url.getGroup();
        return lookupServiceUpdate(group).get(url.getServiceKey());
    }

    private Map<String, List<SalukiURL>> lookupServiceUpdate(String group) {
        Long lastConsulIndexId = lookupGroupServices.get(group) == null ? 0L : lookupGroupServices.get(group);
        String serviceName = toServiceName(group);
        SalukiConsulServiceResp consulResp = client.lookupHealthService(serviceName, lastConsulIndexId);
        if (consulResp != null) {
            List<SalukiConsulService> consulServcies = consulResp.getSalukiConsulServices();
            boolean updated = consulServcies != null && !consulServcies.isEmpty()
                              && consulResp.getConsulIndex() > lastConsulIndexId;
            if (updated) {
                Map<String, List<SalukiURL>> groupProviderUrls = Maps.newConcurrentMap();
                for (SalukiConsulService service : consulServcies) {
                    SalukiURL providerUrl = buildSalukiURL(service);
                    String serviceKey = providerUrl.getServiceKey();
                    List<SalukiURL> urlList = groupProviderUrls.get(serviceKey);
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

    private void notifyListener(SalukiURL url, NotifyListener listener) {
        Map<String, List<SalukiURL>> groupCacheUrls = serviceCache.getIfPresent(url.getGroup());
        if (groupCacheUrls != null) {
            for (Map.Entry<String, List<SalukiURL>> entry : groupCacheUrls.entrySet()) {
                String cacheServiceKey = entry.getKey();
                if (url.getServiceKey().equals(cacheServiceKey)) {
                    List<SalukiURL> newUrls = entry.getValue();
                    ConsulRegistry.this.notify(url, listener, newUrls);
                }
            }
        }
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
                    Map<String, List<SalukiURL>> groupNewUrls = lookupServiceUpdate(group);
                    if (groupNewUrls != null && !groupNewUrls.isEmpty()) {
                        // 缓存中的值
                        Map<String, List<SalukiURL>> groupCacheUrls = serviceCache.getIfPresent(group);
                        if (groupCacheUrls == null) {
                            groupCacheUrls = Maps.newConcurrentMap();
                            serviceCache.put(group, groupCacheUrls);
                        }
                        for (Map.Entry<String, List<SalukiURL>> entry : groupNewUrls.entrySet()) {
                            List<SalukiURL> oldUrls = groupCacheUrls.get(entry.getKey());
                            List<SalukiURL> newUrls = entry.getValue();
                            boolean haveChanged = haveChanged(newUrls, oldUrls);
                            if (haveChanged) {
                                // 更新缓存并且通知监听器
                                groupCacheUrls.put(entry.getKey(), newUrls);
                                Pair<SalukiURL, Set<NotifyListener>> listenerPair = notifyListeners.get(entry.getKey());
                                if (listenerPair != null) {
                                    SalukiURL subscribeUrl = listenerPair.getKey();
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

    private boolean haveChanged(List<SalukiURL> newUrls, List<SalukiURL> oldUrls) {
        if (newUrls == null | newUrls.isEmpty()) {
            return false;
        } else if (oldUrls != null && oldUrls.containsAll(newUrls)) {
            return false;
        }
        return true;
    }

    private SalukiConsulService buildService(SalukiURL url) {
        return SalukiConsulService.newSalukiService()//
                                  .withAddress(url.getHost())//
                                  .withPort(Integer.valueOf(url.getPort()).toString())//
                                  .withName(toServiceName(url.getGroup()))//
                                  .withTag(toUrlPath(url))//
                                  .withId(url.getHost() + ":" + url.getPort() + "-" + url.getPath())//
                                  .withCheckInterval(Integer.valueOf(ConsulConstants.TTL).toString()).build();
    }

    private SalukiConsulEphemralNode buildEphemralNode(SalukiURL url, String flag) {
        return SalukiConsulEphemralNode.newEphemralNode()//
                                       .withHost(url.getHost())//
                                       .withRpcPort(Integer.valueOf(url.getPort()).toString())//
                                       .withFlag(flag)//
                                       .withGroup(url.getGroup())//
                                       .withServiceName(url.getServiceInterface())//
                                       .withCheckInterval(Integer.valueOf(ConsulConstants.TTL).toString()).build();

    }

    private SalukiURL buildSalukiURL(SalukiConsulService service) {
        try {
            for (String tag : service.getTags()) {
                if (StringUtils.indexOf(tag, SalukiConstants.PROVIDERS_CATEGORY) != -1) {
                    String toUrlPath = StringUtils.substringAfter(tag, SalukiConstants.PROVIDERS_CATEGORY);
                    SalukiURL salukiUrl = SalukiURL.valueOf(SalukiURL.decode(toUrlPath));
                    return salukiUrl;
                }
            }
        } catch (Exception e) {
            log.error("convert consul service to url fail! service:" + service, e);
        }
        return null;
    }

    private String toServiceName(String group) {
        return CONSUL_SERVICE_PRE + group;
    }

    private String toServicePath(SalukiURL url) {
        String name = url.getServiceInterface();
        String group = url.getGroup();
        return group + SalukiConstants.PATH_SEPARATOR + SalukiURL.encode(name);
    }

    private String toCategoryPath(SalukiURL url) {
        return toServicePath(url) + SalukiConstants.PATH_SEPARATOR + SalukiConstants.PROVIDERS_CATEGORY;
    }

    private String toUrlPath(SalukiURL url) {
        return toCategoryPath(url) + SalukiConstants.PATH_SEPARATOR + SalukiURL.encode(url.toFullString());
    }
}
