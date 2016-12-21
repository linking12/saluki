/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
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
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.core.common.ThrallURL;
import com.quancheng.saluki.core.common.ThrallURLUtils;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.Registry;

/**
 * @author shimingliu 2016年12月14日 下午1:55:28
 * @version AbstractRegistry1.java, v 0.0.1 2016年12月14日 下午1:55:28 shimingliu
 */
public abstract class AbstractRegistry implements Registry {

    protected final Logger                                                       logger     = LoggerFactory.getLogger(getClass());
    private final ThrallURL                                                      registryUrl;
    private final Set<ThrallURL>                                                 registered = Sets.newConcurrentHashSet();
    private final ConcurrentMap<ThrallURL, Set<NotifyListener>>                  subscribed = Maps.newConcurrentMap();
    private final ConcurrentMap<ThrallURL, Map<NotifyListener, List<ThrallURL>>> notified   = Maps.newConcurrentMap();
    private final ExecutorService                                                notifyExecutor;
    private final int                                                            cpus       = Runtime.getRuntime().availableProcessors();

    public AbstractRegistry(ThrallURL registryUrl){
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

    public ThrallURL getRegistryUrl() {
        return registryUrl;
    }

    public Set<ThrallURL> getRegistered() {
        return registered;
    }

    public Map<ThrallURL, Set<NotifyListener>> getSubscribed() {
        return subscribed;
    }

    public Map<ThrallURL, Map<NotifyListener, List<ThrallURL>>> getNotified() {
        return notified;
    }

    public List<ThrallURL> discover(ThrallURL url) {
        String[] keys = new String[] { Constants.ASYNC_KEY, Constants.GENERIC_KEY, Constants.TIMEOUT };
        url = url.removeParameters(keys);
        List<ThrallURL> result = new ArrayList<ThrallURL>();
        Map<NotifyListener, List<ThrallURL>> notifiedUrls = getNotified().get(url);
        if (notifiedUrls != null && notifiedUrls.size() > 0) {
            for (List<ThrallURL> urls : notifiedUrls.values()) {
                for (ThrallURL u : urls) {
                    result.add(u);
                }
            }
        } else {
            final AtomicReference<List<ThrallURL>> reference = new AtomicReference<List<ThrallURL>>();
            NotifyListener listener = new NotifyListener() {

                public void notify(List<ThrallURL> urls) {
                    reference.set(urls);
                }

            };
            subscribe(url, listener); // 订阅逻辑保证第一次notify后再返回
            List<ThrallURL> urls = reference.get();
            if (urls != null && urls.size() > 0) {
                for (ThrallURL u : urls) {
                    result.add(u);
                }
            }
        }
        return result;
    }

    public void register(ThrallURL url) {
        if (url == null) {
            throw new IllegalArgumentException("register url == null");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Register: " + url);
        }
        registered.add(url);
    }

    public void unregister(ThrallURL url) {
        if (url == null) {
            throw new IllegalArgumentException("unregister url == null");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Unregister: " + url);
        }
        registered.remove(url);
    }

    public void subscribe(ThrallURL url, NotifyListener listener) {
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
        Set<NotifyListener> listeners = subscribed.get(url);
        if (listeners == null) {
            subscribed.putIfAbsent(url, Sets.newConcurrentHashSet());
            listeners = subscribed.get(url);
        }
        listeners.add(listener);
    }

    public void unsubscribe(ThrallURL url, NotifyListener listener) {
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

    protected void notify(List<ThrallURL> providerUrls) {
        if (providerUrls == null || providerUrls.isEmpty()) return;
        for (Map.Entry<ThrallURL, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
            ThrallURL subscribedUrl = entry.getKey();
            if (!ThrallURLUtils.isMatch(subscribedUrl, providerUrls.get(0))) {
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

    protected void notify(ThrallURL subscribedUrl, NotifyListener listener, List<ThrallURL> providerUrls) {
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
        Set<ThrallURL> recoverRegistered = Sets.newHashSet(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover register url " + recoverRegistered);
            }
            for (ThrallURL url : recoverRegistered) {
                register(url);
            }
        }
        // subscribe
        Map<ThrallURL, Set<NotifyListener>> recoverSubscribed = Maps.newHashMap(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover subscribe url " + recoverSubscribed.keySet());
            }
            for (Map.Entry<ThrallURL, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                ThrallURL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    subscribe(url, listener);
                }
            }
        }
    }

    private void addNotified(ThrallURL subscribedUrl, NotifyListener listener, List<ThrallURL> providerUrls) {
        Map<NotifyListener, List<ThrallURL>> notifiedUrlMap = notified.get(subscribedUrl);
        List<ThrallURL> notifiedUrlList;
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
