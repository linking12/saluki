package com.quancheng.saluki.core.registry.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.core.utils.NamedThreadFactory;
import com.quancheng.saluki.core.utils.UrlUtils;

public abstract class AbstractRegistry implements Registry {

    protected final Logger                                                       logger     = LoggerFactory.getLogger(getClass());
    private final SalukiURL                                                      registryUrl;
    private final Set<SalukiURL>                                                 registered = Sets.newConcurrentHashSet();
    private final ConcurrentMap<SalukiURL, Set<NotifyListener>>                  subscribed = Maps.newConcurrentMap();
    private final ConcurrentMap<SalukiURL, Map<NotifyListener, List<SalukiURL>>> notified   = Maps.newConcurrentMap();
    private final ExecutorService                                                notifyExecutor;
    private final int                                                            cpus       = Runtime.getRuntime().availableProcessors();

    public AbstractRegistry(SalukiURL registryUrl){
        if (registryUrl == null) {
            throw new IllegalArgumentException("registry url == null");
        }
        this.registryUrl = registryUrl;
        this.notifyExecutor = Executors.newFixedThreadPool(cpus * 3,
                                                           new NamedThreadFactory("SalukiNotifyListener", true));
    }

    public int getCpus() {
        return cpus;
    }

    public SalukiURL getRegistryUrl() {
        return registryUrl;
    }

    public Set<SalukiURL> getRegistered() {
        return registered;
    }

    public Map<SalukiURL, Set<NotifyListener>> getSubscribed() {
        return subscribed;
    }

    public Map<SalukiURL, Map<NotifyListener, List<SalukiURL>>> getNotified() {
        return notified;
    }

    public List<SalukiURL> discover(SalukiURL url) {
        List<SalukiURL> result = new ArrayList<SalukiURL>();
        Map<NotifyListener, List<SalukiURL>> notifiedUrls = getNotified().get(url);
        if (notifiedUrls != null && notifiedUrls.size() > 0) {
            for (List<SalukiURL> urls : notifiedUrls.values()) {
                for (SalukiURL u : urls) {
                    result.add(u);
                }
            }
        } else {
            final AtomicReference<List<SalukiURL>> reference = new AtomicReference<List<SalukiURL>>();
            NotifyListener listener = new NotifyListener() {

                public void notify(List<SalukiURL> urls) {
                    reference.set(urls);
                }

            };
            subscribe(url, listener); // 订阅逻辑保证第一次notify后再返回
            List<SalukiURL> urls = reference.get();
            if (urls != null && urls.size() > 0) {
                for (SalukiURL u : urls) {
                    result.add(u);
                }
            }
        }
        return result;
    }

    public void register(SalukiURL url) {
        if (url == null) {
            throw new IllegalArgumentException("register url == null");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Register: " + url);
        }
        registered.add(url);
    }

    public void unregister(SalukiURL url) {
        if (url == null) {
            throw new IllegalArgumentException("unregister url == null");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Unregister: " + url);
        }
        registered.remove(url);
    }

    public void subscribe(SalukiURL url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("subscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener == null");
        }
        String[] keys = new String[] { SalukiConstants.GRPC_IN_LOCAL_PROCESS, SalukiConstants.RPCTYPE_KEY,
                                       SalukiConstants.GENERIC_KEY, SalukiConstants.RPCTIMEOUT_KEY };
        url = url.removeParameters(keys);
        if (logger.isInfoEnabled()) {
            logger.info("Subscribe: " + url);
        }
        Set<NotifyListener> listeners = subscribed.get(url);
        if (listeners == null) {
            subscribed.putIfAbsent(url, Sets.newConcurrentHashSet());
            listeners = subscribed.get(url);
        }
        listeners.add(listener);
    }

    public void unsubscribe(SalukiURL url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("unsubscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("unsubscribe listener == null");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Unsubscribe: " + url);
        }
        Set<NotifyListener> listeners = subscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    protected void notify(List<SalukiURL> providerUrls) {
        if (providerUrls == null || providerUrls.isEmpty()) return;
        for (Map.Entry<SalukiURL, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
            SalukiURL subscribedUrl = entry.getKey();
            if (!UrlUtils.isMatch(subscribedUrl, providerUrls.get(0))) {
                continue;
            }
            Set<NotifyListener> listeners = entry.getValue();
            if (listeners != null) {
                for (NotifyListener listener : listeners) {
                    notify(subscribedUrl, listener, providerUrls);
                }
            }
        }
    }

    protected void notify(SalukiURL subscribedUrl, NotifyListener listener, List<SalukiURL> providerUrls) {
        addNotified(subscribedUrl, listener, providerUrls);
        notifyExecutor.submit(new Runnable() {

            @Override
            public void run() {
                listener.notify(providerUrls);

            }
        });

    }

    protected void recover() throws Exception {
        // register
        Set<SalukiURL> recoverRegistered = Sets.newHashSet(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover register url " + recoverRegistered);
            }
            for (SalukiURL url : recoverRegistered) {
                register(url);
            }
        }
        // subscribe
        Map<SalukiURL, Set<NotifyListener>> recoverSubscribed = Maps.newHashMap(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover subscribe url " + recoverSubscribed.keySet());
            }
            for (Map.Entry<SalukiURL, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                SalukiURL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    subscribe(url, listener);
                }
            }
        }
    }

    private void addNotified(SalukiURL subscribedUrl, NotifyListener listener, List<SalukiURL> providerUrls) {
        Map<NotifyListener, List<SalukiURL>> notifiedUrlMap = notified.get(subscribedUrl);
        List<SalukiURL> notifiedUrlList;
        if (notifiedUrlMap == null) {
            notifiedUrlMap = Maps.newConcurrentMap();
            notifiedUrlList = providerUrls;
        } else {
            notifiedUrlList = notifiedUrlMap.get(listener);
            if (notifiedUrlList == null) {
                notifiedUrlList = Lists.newArrayList();
            }
            notifiedUrlList.addAll(providerUrls);
        }
        notifiedUrlMap.putIfAbsent(listener, notifiedUrlList);
        notified.putIfAbsent(subscribedUrl, notifiedUrlMap);
    }

}
