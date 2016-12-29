/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.registry.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.core.registry.NotifyListener;

/**
 * @author shimingliu 2016年12月14日 下午1:55:42
 * @version FailBackRegistry1.java, v 0.0.1 2016年12月14日 下午1:55:42 shimingliu
 */
public abstract class FailbackRegistry extends AbstractRegistry {

    // 定时任务执行器
    private final ScheduledExecutorService                                                         retryExecutor             = Executors.newScheduledThreadPool(1,
                                                                                                                                                                new NamedThreadFactory("SalukiRegistryFailedRetryTimer",
                                                                                                                                                                                       true));
    // 失败重试定时器，定时检查是否有请求失败，如有，无限次重试
    private final ScheduledFuture<?>                                                               retryFuture;

    private final Set<GrpcURL>                                                                     failedRegistered          = Sets.newConcurrentHashSet();

    private final Set<GrpcURL>                                                                     failedUnregistered        = Sets.newConcurrentHashSet();

    private final ConcurrentMap<GrpcURL, Set<NotifyListener.NotifyServiceListener>>                failedServiceSubscribed   = Maps.newConcurrentMap();

    private final ConcurrentMap<GrpcURL, Set<NotifyListener.NotifyServiceListener>>                failedServiceUnsubscribed = Maps.newConcurrentMap();

    private final ConcurrentMap<GrpcURL, Set<NotifyListener.NotifyRouterListener>>                 failedRouterSubscribed    = Maps.newConcurrentMap();

    private final ConcurrentMap<GrpcURL, Set<NotifyListener.NotifyRouterListener>>                 failedRouterUnsubscribed  = Maps.newConcurrentMap();

    private final ConcurrentMap<GrpcURL, Map<NotifyListener.NotifyServiceListener, List<GrpcURL>>> failedServiceNotified     = Maps.newConcurrentMap();

    private final ConcurrentMap<GrpcURL, Map<NotifyListener.NotifyRouterListener, String>>         failedRouterNotified      = Maps.newConcurrentMap();

    public FailbackRegistry(GrpcURL url){
        super(url);
        int retryPeriod = url.getParameter(Constants.REGISTRY_RETRY_PERIOD_KEY,
                                           Constants.DEFAULT_REGISTRY_RETRY_PERIOD);
        this.retryFuture = retryExecutor.scheduleWithFixedDelay(new Runnable() {

            public void run() {
                // 检测并连接注册中心
                try {
                    retry();
                } catch (Throwable t) { // 防御性容错
                    logger.error("Unexpected error occur at failed retry, cause: " + t.getMessage(), t);
                }
            }
        }, retryPeriod, retryPeriod, TimeUnit.MILLISECONDS);
    }

    private void retry() {
        if (!failedRegistered.isEmpty()) {
            Set<GrpcURL> failed = new HashSet<GrpcURL>(failedRegistered);
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry register " + failed);
                }
                try {
                    for (GrpcURL url : failed) {
                        try {
                            doRegister(url);
                            failedRegistered.remove(url);
                        } catch (Throwable t) { // 忽略所有异常，等待下次重试
                            logger.warn("Failed to retry register " + failed + ", waiting for again, cause: "
                                        + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    logger.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(),
                                t);
                }
            }
        }
        if (!failedUnregistered.isEmpty()) {
            Set<GrpcURL> failed = new HashSet<GrpcURL>(failedUnregistered);
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry unregister " + failed);
                }
                try {
                    for (GrpcURL url : failed) {
                        try {
                            doUnregister(url);
                            failedUnregistered.remove(url);
                        } catch (Throwable t) { // 忽略所有异常，等待下次重试
                            logger.warn("Failed to retry unregister  " + failed + ", waiting for again, cause: "
                                        + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    logger.warn("Failed to retry unregister  " + failed + ", waiting for again, cause: "
                                + t.getMessage(), t);
                }
            }
        }
        if (!failedServiceSubscribed.isEmpty()) {
            Map<GrpcURL, Set<NotifyListener.NotifyServiceListener>> failed = new HashMap<GrpcURL, Set<NotifyListener.NotifyServiceListener>>(failedServiceSubscribed);
            for (Map.Entry<GrpcURL, Set<NotifyListener.NotifyServiceListener>> entry : new HashMap<GrpcURL, Set<NotifyListener.NotifyServiceListener>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry subscribe " + failed);
                }
                try {
                    for (Map.Entry<GrpcURL, Set<NotifyListener.NotifyServiceListener>> entry : failed.entrySet()) {
                        GrpcURL url = entry.getKey();
                        Set<NotifyListener.NotifyServiceListener> listeners = entry.getValue();
                        for (NotifyListener.NotifyServiceListener listener : listeners) {
                            try {
                                doSubscribe(url, listener);
                                listeners.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                logger.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: "
                                            + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    logger.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(),
                                t);
                }
            }
        }
        if (!failedServiceUnsubscribed.isEmpty()) {
            Map<GrpcURL, Set<NotifyListener.NotifyServiceListener>> failed = new HashMap<GrpcURL, Set<NotifyListener.NotifyServiceListener>>(failedServiceUnsubscribed);
            for (Map.Entry<GrpcURL, Set<NotifyListener.NotifyServiceListener>> entry : new HashMap<GrpcURL, Set<NotifyListener.NotifyServiceListener>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry unsubscribe " + failed);
                }
                try {
                    for (Map.Entry<GrpcURL, Set<NotifyListener.NotifyServiceListener>> entry : failed.entrySet()) {
                        GrpcURL url = entry.getKey();
                        Set<NotifyListener.NotifyServiceListener> listeners = entry.getValue();
                        for (NotifyListener.NotifyServiceListener listener : listeners) {
                            try {
                                doUnsubscribe(url, listener);
                                listeners.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                logger.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: "
                                            + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    logger.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: "
                                + t.getMessage(), t);
                }
            }
        }
        if (!failedServiceNotified.isEmpty()) {
            Map<GrpcURL, Map<NotifyListener.NotifyServiceListener, List<GrpcURL>>> failed = new HashMap<GrpcURL, Map<NotifyListener.NotifyServiceListener, List<GrpcURL>>>(failedServiceNotified);
            for (Map.Entry<GrpcURL, Map<NotifyListener.NotifyServiceListener, List<GrpcURL>>> entry : new HashMap<GrpcURL, Map<NotifyListener.NotifyServiceListener, List<GrpcURL>>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry notify " + failed);
                }
                try {
                    for (Map<NotifyListener.NotifyServiceListener, List<GrpcURL>> values : failed.values()) {
                        for (Map.Entry<NotifyListener.NotifyServiceListener, List<GrpcURL>> entry : values.entrySet()) {
                            try {
                                NotifyListener.NotifyServiceListener listener = entry.getKey();
                                List<GrpcURL> urls = entry.getValue();
                                listener.notify(urls);
                                values.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                logger.warn("Failed to retry notify " + failed + ", waiting for again, cause: "
                                            + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    logger.warn("Failed to retry notify " + failed + ", waiting for again, cause: " + t.getMessage(),
                                t);
                }
            }
        }

        // Router

        if (!failedRouterSubscribed.isEmpty()) {
            Map<GrpcURL, Set<NotifyListener.NotifyRouterListener>> failed = new HashMap<GrpcURL, Set<NotifyListener.NotifyRouterListener>>(failedRouterSubscribed);
            for (Map.Entry<GrpcURL, Set<NotifyListener.NotifyRouterListener>> entry : new HashMap<GrpcURL, Set<NotifyListener.NotifyRouterListener>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry subscribe " + failed);
                }
                try {
                    for (Map.Entry<GrpcURL, Set<NotifyListener.NotifyRouterListener>> entry : failed.entrySet()) {
                        GrpcURL url = entry.getKey();
                        Set<NotifyListener.NotifyRouterListener> listeners = entry.getValue();
                        for (NotifyListener.NotifyRouterListener listener : listeners) {
                            try {
                                doSubscribe(url, listener);
                                listeners.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                logger.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: "
                                            + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    logger.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(),
                                t);
                }
            }
        }
        if (!failedRouterUnsubscribed.isEmpty()) {
            Map<GrpcURL, Set<NotifyListener.NotifyRouterListener>> failed = new HashMap<GrpcURL, Set<NotifyListener.NotifyRouterListener>>(failedRouterUnsubscribed);
            for (Map.Entry<GrpcURL, Set<NotifyListener.NotifyRouterListener>> entry : new HashMap<GrpcURL, Set<NotifyListener.NotifyRouterListener>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry unsubscribe " + failed);
                }
                try {
                    for (Map.Entry<GrpcURL, Set<NotifyListener.NotifyRouterListener>> entry : failed.entrySet()) {
                        GrpcURL url = entry.getKey();
                        Set<NotifyListener.NotifyRouterListener> listeners = entry.getValue();
                        for (NotifyListener.NotifyRouterListener listener : listeners) {
                            try {
                                doUnsubscribe(url, listener);
                                listeners.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                logger.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: "
                                            + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    logger.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: "
                                + t.getMessage(), t);
                }
            }
        }
        if (!failedRouterNotified.isEmpty()) {
            Map<GrpcURL, Map<NotifyListener.NotifyRouterListener, String>> failed = new HashMap<GrpcURL, Map<NotifyListener.NotifyRouterListener, String>>(failedRouterNotified);
            for (Map.Entry<GrpcURL, Map<NotifyListener.NotifyRouterListener, String>> entry : new HashMap<GrpcURL, Map<NotifyListener.NotifyRouterListener, String>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry notify " + failed);
                }
                try {
                    for (Map<NotifyListener.NotifyRouterListener, String> values : failed.values()) {
                        for (Map.Entry<NotifyListener.NotifyRouterListener, String> entry : values.entrySet()) {
                            try {
                                NotifyListener.NotifyRouterListener listener = entry.getKey();
                                String routerMessage = entry.getValue();
                                listener.notify(routerMessage);
                                values.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                logger.warn("Failed to retry notify " + failed + ", waiting for again, cause: "
                                            + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    logger.warn("Failed to retry notify " + failed + ", waiting for again, cause: " + t.getMessage(),
                                t);
                }
            }
        }
    }

    public Future<?> getRetryFuture() {
        return retryFuture;
    }

    public Set<GrpcURL> getFailedRegistered() {
        return failedRegistered;
    }

    public Set<GrpcURL> getFailedUnregistered() {
        return failedUnregistered;
    }

    public Map<GrpcURL, Set<NotifyListener.NotifyServiceListener>> getFailedServiceSubscribed() {
        return failedServiceSubscribed;
    }

    public Map<GrpcURL, Set<NotifyListener.NotifyServiceListener>> getFailedServiceUnsubscribed() {
        return failedServiceUnsubscribed;
    }

    public Map<GrpcURL, Map<NotifyListener.NotifyServiceListener, List<GrpcURL>>> getFailedServiceNotified() {
        return failedServiceNotified;
    }

    public Map<GrpcURL, Set<NotifyListener.NotifyRouterListener>> getFailedRouterSubscribed() {
        return failedRouterSubscribed;
    }

    public Map<GrpcURL, Set<NotifyListener.NotifyRouterListener>> getFailedRouterUnsubscribed() {
        return failedRouterUnsubscribed;
    }

    public Map<GrpcURL, Map<NotifyListener.NotifyRouterListener, String>> getFailedRouterNotified() {
        return failedRouterNotified;
    }

    private void addFailedSubscribed(GrpcURL url, NotifyListener.NotifyServiceListener listener) {
        Set<NotifyListener.NotifyServiceListener> listeners = failedServiceSubscribed.get(url);
        if (listeners == null) {
            listeners = Sets.newConcurrentHashSet();
            listeners = failedServiceSubscribed.putIfAbsent(url, listeners);
        }
        listeners.add(listener);
    }

    private void removeFailedSubscribed(GrpcURL url, NotifyListener.NotifyServiceListener listener) {
        Set<NotifyListener.NotifyServiceListener> listeners = failedServiceSubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        listeners = failedServiceUnsubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        Map<NotifyListener.NotifyServiceListener, List<GrpcURL>> notified = failedServiceNotified.get(url);
        if (notified != null) {
            notified.remove(listener);
        }
    }

    private void addFailedSubscribed(GrpcURL url, NotifyListener.NotifyRouterListener listener) {
        Set<NotifyListener.NotifyRouterListener> listeners = failedRouterSubscribed.get(url);
        if (listeners == null) {
            listeners = Sets.newConcurrentHashSet();
            listeners = failedRouterSubscribed.putIfAbsent(url, listeners);
        }
        listeners.add(listener);
    }

    private void removeFailedSubscribed(GrpcURL url, NotifyListener.NotifyRouterListener listener) {
        Set<NotifyListener.NotifyRouterListener> listeners = failedRouterSubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        listeners = failedRouterSubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        Map<NotifyListener.NotifyRouterListener, String> notified = failedRouterNotified.get(url);
        if (notified != null) {
            notified.remove(listener);
        }
    }

    @Override
    public void register(GrpcURL url) {
        super.register(url);
        failedRegistered.remove(url);
        failedUnregistered.remove(url);
        try {
            // 向服务器端发送注册请求
            doRegister(url);
        } catch (Exception e) {
            logger.error("Failed to uregister " + url + ", waiting for retry, cause: " + e.getMessage(), e);
            // 将失败的取消注册请求记录到失败列表，定时重试
            failedUnregistered.add(url);
        }
    }

    @Override
    public void unregister(GrpcURL url) {
        super.unregister(url);
        failedRegistered.remove(url);
        failedUnregistered.remove(url);
        try {
            // 向服务器端发送取消注册请求
            doUnregister(url);
        } catch (Exception e) {
            logger.error("Failed to uregister " + url + ", waiting for retry, cause: " + e.getMessage(), e);
            // 将失败的取消注册请求记录到失败列表，定时重试
            failedUnregistered.add(url);
        }
    }

    @Override
    public void subscribe(GrpcURL url, NotifyListener.NotifyServiceListener listener) {
        super.subscribe(url, listener);
        removeFailedSubscribed(url, listener);
        try {
            // 向服务器端发送订阅请求
            doSubscribe(url, listener);
        } catch (Exception e) {
            logger.error("Failed to subscribe " + url + ", waiting for retry, cause: " + e.getMessage(), e);
            // 将失败的订阅请求记录到失败列表，定时重试
            addFailedSubscribed(url, listener);
        }
    }

    @Override
    public void unsubscribe(GrpcURL url, NotifyListener.NotifyServiceListener listener) {
        super.unsubscribe(url, listener);
        removeFailedSubscribed(url, listener);
        try {
            // 向服务器端发送取消订阅请求
            doUnsubscribe(url, listener);
        } catch (Exception e) {
            logger.error("Failed to unsubscribe " + url + ", waiting for retry, cause: " + e.getMessage(), e);
            // 将失败的取消订阅请求记录到失败列表，定时重试
            Set<NotifyListener.NotifyServiceListener> listeners = failedServiceUnsubscribed.get(url);
            if (listeners == null) {
                listeners = Sets.newConcurrentHashSet();
                listeners = failedServiceUnsubscribed.putIfAbsent(url, listeners);
            }
            listeners.add(listener);
        }
    }

    @Override
    public void subscribe(GrpcURL url, NotifyListener.NotifyRouterListener listener) {
        super.subscribe(url, listener);
        removeFailedSubscribed(url, listener);
        try {
            // 向服务器端发送订阅请求
            doSubscribe(url, listener);
        } catch (Exception e) {
            logger.error("Failed to subscribe " + url + ", waiting for retry, cause: " + e.getMessage(), e);
            // 将失败的订阅请求记录到失败列表，定时重试
            addFailedSubscribed(url, listener);
        }
    }

    @Override
    public void unsubscribe(GrpcURL url, NotifyListener.NotifyRouterListener listener) {
        super.unsubscribe(url, listener);
        removeFailedSubscribed(url, listener);
        try {
            // 向服务器端发送取消订阅请求
            doUnsubscribe(url, listener);
        } catch (Exception e) {
            logger.error("Failed to unsubscribe " + url + ", waiting for retry, cause: " + e.getMessage(), e);
            // 将失败的取消订阅请求记录到失败列表，定时重试
            Set<NotifyListener.NotifyRouterListener> listeners = failedRouterUnsubscribed.get(url);
            if (listeners == null) {
                listeners = Sets.newConcurrentHashSet();
                listeners = failedRouterUnsubscribed.putIfAbsent(url, listeners);
            }
            listeners.add(listener);
        }
    }

    @Override
    protected void notify(GrpcURL url, NotifyListener.NotifyServiceListener listener, List<GrpcURL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        try {
            doNotifyService(url, listener, urls);
        } catch (Exception t) {
            // 将失败的通知请求记录到失败列表，定时重试
            Map<NotifyListener.NotifyServiceListener, List<GrpcURL>> listeners = failedServiceNotified.get(url);
            if (listeners == null) {
                failedServiceNotified.putIfAbsent(url, Maps.newConcurrentMap());
                listeners = failedServiceNotified.get(url);
            }
            listeners.put(listener, urls);
            logger.error("Failed to notify for subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
        }
    }

    @Override
    protected void notify(GrpcURL url, NotifyListener.NotifyRouterListener listener, String routerMessage) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        try {
            doNotifyRouter(url, listener, routerMessage);
        } catch (Exception t) {
            // 将失败的通知请求记录到失败列表，定时重试
            Map<NotifyListener.NotifyRouterListener, String> listeners = failedRouterNotified.get(url);
            if (listeners == null) {
                failedRouterNotified.putIfAbsent(url, Maps.newConcurrentMap());
                listeners = failedRouterNotified.get(url);
            }
            listeners.put(listener, routerMessage);
            logger.error("Failed to notify for subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
        }
    }

    protected void doNotifyService(GrpcURL url, NotifyListener.NotifyServiceListener listener, List<GrpcURL> urls) {
        super.notify(url, listener, urls);
    }

    protected void doNotifyRouter(GrpcURL url, NotifyListener.NotifyRouterListener listener, String routerMessage) {
        super.notify(url, listener, routerMessage);
    }

    @Override
    protected void recover() throws Exception {
        // register
        Set<GrpcURL> recoverRegistered = Sets.newHashSet(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover register url " + recoverRegistered);
            }
            for (GrpcURL url : recoverRegistered) {
                failedRegistered.add(url);
            }
        }
        // subscribe
        Map<GrpcURL, Set<NotifyListener.NotifyServiceListener>> recoverSubscribed = Maps.newHashMap(getServiceSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover subscribe url " + recoverSubscribed.keySet());
            }
            for (Map.Entry<GrpcURL, Set<NotifyListener.NotifyServiceListener>> entry : recoverSubscribed.entrySet()) {
                GrpcURL url = entry.getKey();
                for (NotifyListener.NotifyServiceListener listener : entry.getValue()) {
                    addFailedSubscribed(url, listener);
                }
            }
        }
    }

    // ==== 模板方法 ====
    protected abstract void doRegister(GrpcURL url);

    protected abstract void doUnregister(GrpcURL url);

    protected abstract void doSubscribe(GrpcURL url, NotifyListener.NotifyServiceListener listener);

    protected abstract void doUnsubscribe(GrpcURL url, NotifyListener.NotifyServiceListener listener);

    protected abstract void doSubscribe(GrpcURL url, NotifyListener.NotifyRouterListener listener);

    protected abstract void doUnsubscribe(GrpcURL url, NotifyListener.NotifyRouterListener listener);
}
