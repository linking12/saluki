/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.registry.internal;

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
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.common.GrpcURLUtils;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.Registry;

/**
 * @author shimingliu 2016年12月14日 下午1:55:28
 * @version AbstractRegistry1.java, v 0.0.1 2016年12月14日 下午1:55:28 shimingliu
 */
public abstract class AbstractRegistry implements Registry {

    protected final Logger                                                   logger     = LoggerFactory.getLogger(getClass());
    private final GrpcURL                                                    registryUrl;
    private final Set<GrpcURL>                                               registered = Sets.newConcurrentHashSet();
    private final ConcurrentMap<GrpcURL, Set<NotifyListener.NotifyServiceListener>>                subscribed = Maps.newConcurrentMap();
    private final ConcurrentMap<GrpcURL, Map<NotifyListener.NotifyServiceListener, List<GrpcURL>>> notified   = Maps.newConcurrentMap();
    private final ExecutorService                                            notifyExecutor;
    private final int                                                        cpus       = Runtime.getRuntime().availableProcessors();

    public AbstractRegistry(GrpcURL registryUrl){
        if (registryUrl == null) {
            throw new IllegalArgumentException("registry url == null");
        }
        this.registryUrl = registryUrl;
        this.notifyExecutor = Executors.newFixedThreadPool(cpus * 3,
                                                           new NamedThreadFactory("SalukiNotifyListener.NotifyServiceListener", true));
    }

    public int getCpus() {
        return cpus;
    }

    public GrpcURL getRegistryUrl() {
        return registryUrl;
    }

    public Set<GrpcURL> getRegistered() {
        return registered;
    }

    public Map<GrpcURL, Set<NotifyListener.NotifyServiceListener>> getSubscribed() {
        return subscribed;
    }

    public Map<GrpcURL, Map<NotifyListener.NotifyServiceListener, List<GrpcURL>>> getNotified() {
        return notified;
    }

    public List<GrpcURL> discover(GrpcURL url) {
        String[] keys = new String[] { Constants.ASYNC_KEY, Constants.GENERIC_KEY, Constants.TIMEOUT };
        url = url.removeParameters(keys);
        List<GrpcURL> result = new ArrayList<GrpcURL>();
        Map<NotifyListener.NotifyServiceListener, List<GrpcURL>> notifiedUrls = getNotified().get(url);
        if (notifiedUrls != null && notifiedUrls.size() > 0) {
            for (List<GrpcURL> urls : notifiedUrls.values()) {
                for (GrpcURL u : urls) {
                    result.add(u);
                }
            }
        } else {
            final AtomicReference<List<GrpcURL>> reference = new AtomicReference<List<GrpcURL>>();
            NotifyListener.NotifyServiceListener listener = new NotifyListener.NotifyServiceListener() {

                public void notify(List<GrpcURL> urls) {
                    reference.set(urls);
                }
            };
            subscribe(url, listener); // 订阅逻辑保证第一次notify后再返回
            List<GrpcURL> urls = reference.get();
            if (urls != null && urls.size() > 0) {
                for (GrpcURL u : urls) {
                    result.add(u);
                }
            }
        }
        return result;
    }

    public void register(GrpcURL url) {
        if (url == null) {
            throw new IllegalArgumentException("register url == null");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Register: " + url);
        }
        registered.add(url);
    }

    public void unregister(GrpcURL url) {
        if (url == null) {
            throw new IllegalArgumentException("unregister url == null");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Unregister: " + url);
        }
        registered.remove(url);
    }

    public void subscribe(GrpcURL url, NotifyListener.NotifyServiceListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("subscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener == null");
        }
        String[] keys = new String[] { Constants.ASYNC_KEY, Constants.GENERIC_KEY, Constants.TIMEOUT };
        url = url.removeParameters(keys);
        if (logger.isInfoEnabled()) {
            logger.info("Subscribe: " + url);
        }
        Set<NotifyListener.NotifyServiceListener> listeners = subscribed.get(url);
        if (listeners == null) {
            subscribed.putIfAbsent(url, Sets.newConcurrentHashSet());
            listeners = subscribed.get(url);
        }
        listeners.add(listener);
    }

    public void unsubscribe(GrpcURL url, NotifyListener.NotifyServiceListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("unsubscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("unsubscribe listener == null");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Unsubscribe: " + url);
        }
        Set<NotifyListener.NotifyServiceListener> listeners = subscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    protected void notify(List<GrpcURL> providerUrls) {
        if (providerUrls == null || providerUrls.isEmpty()) return;
        for (Map.Entry<GrpcURL, Set<NotifyListener.NotifyServiceListener>> entry : getSubscribed().entrySet()) {
            GrpcURL subscribedUrl = entry.getKey();
            if (!GrpcURLUtils.isMatch(subscribedUrl, providerUrls.get(0))) {
                continue;
            }
            Set<NotifyListener.NotifyServiceListener> listeners = entry.getValue();
            if (listeners != null) {
                for (NotifyListener.NotifyServiceListener listener : listeners) {
                    notify(subscribedUrl, listener, providerUrls);
                }
            }
        }
    }

    protected void notify(GrpcURL subscribedUrl, NotifyListener.NotifyServiceListener listener, List<GrpcURL> providerUrls) {
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
        Set<GrpcURL> recoverRegistered = Sets.newHashSet(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover register url " + recoverRegistered);
            }
            for (GrpcURL url : recoverRegistered) {
                register(url);
            }
        }
        // subscribe
        Map<GrpcURL, Set<NotifyListener.NotifyServiceListener>> recoverSubscribed = Maps.newHashMap(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover subscribe url " + recoverSubscribed.keySet());
            }
            for (Map.Entry<GrpcURL, Set<NotifyListener.NotifyServiceListener>> entry : recoverSubscribed.entrySet()) {
                GrpcURL url = entry.getKey();
                for (NotifyListener.NotifyServiceListener listener : entry.getValue()) {
                    subscribe(url, listener);
                }
            }
        }
    }

    private void addNotified(GrpcURL subscribedUrl, NotifyListener.NotifyServiceListener listener, List<GrpcURL> providerUrls) {
        Map<NotifyListener.NotifyServiceListener, List<GrpcURL>> notifiedUrlMap = notified.get(subscribedUrl);
        List<GrpcURL> notifiedUrlList;
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
