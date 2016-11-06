package com.quancheng.saluki.core.registry.support;

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
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.utils.NamedThreadFactory;

public abstract class FailbackRegistry extends AbstractRegistry {

    // 定时任务执行器
    private final ScheduledExecutorService                                       retryExecutor      = Executors.newScheduledThreadPool(1,
                                                                                                                                       new NamedThreadFactory("SalukiRegistryFailedRetryTimer",
                                                                                                                                                              true));
    // 失败重试定时器，定时检查是否有请求失败，如有，无限次重试
    private final ScheduledFuture<?>                                             retryFuture;

    private final Set<SalukiURL>                                                 failedRegistered   = Sets.newConcurrentHashSet();

    private final Set<SalukiURL>                                                 failedUnregistered = Sets.newConcurrentHashSet();

    private final ConcurrentMap<SalukiURL, Set<NotifyListener>>                  failedSubscribed   = Maps.newConcurrentMap();

    private final ConcurrentMap<SalukiURL, Set<NotifyListener>>                  failedUnsubscribed = Maps.newConcurrentMap();

    private final ConcurrentMap<SalukiURL, Map<NotifyListener, List<SalukiURL>>> failedNotified     = Maps.newConcurrentMap();

    public FailbackRegistry(SalukiURL url){
        super(url);
        int retryPeriod = url.getParameter(SalukiConstants.REGISTRY_RETRY_PERIOD_KEY,
                                           SalukiConstants.DEFAULT_REGISTRY_RETRY_PERIOD);
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
            Set<SalukiURL> failed = new HashSet<SalukiURL>(failedRegistered);
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry register " + failed);
                }
                try {
                    for (SalukiURL url : failed) {
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
            Set<SalukiURL> failed = new HashSet<SalukiURL>(failedUnregistered);
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry unregister " + failed);
                }
                try {
                    for (SalukiURL url : failed) {
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
        if (!failedSubscribed.isEmpty()) {
            Map<SalukiURL, Set<NotifyListener>> failed = new HashMap<SalukiURL, Set<NotifyListener>>(failedSubscribed);
            for (Map.Entry<SalukiURL, Set<NotifyListener>> entry : new HashMap<SalukiURL, Set<NotifyListener>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry subscribe " + failed);
                }
                try {
                    for (Map.Entry<SalukiURL, Set<NotifyListener>> entry : failed.entrySet()) {
                        SalukiURL url = entry.getKey();
                        Set<NotifyListener> listeners = entry.getValue();
                        for (NotifyListener listener : listeners) {
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
        if (!failedUnsubscribed.isEmpty()) {
            Map<SalukiURL, Set<NotifyListener>> failed = new HashMap<SalukiURL, Set<NotifyListener>>(failedUnsubscribed);
            for (Map.Entry<SalukiURL, Set<NotifyListener>> entry : new HashMap<SalukiURL, Set<NotifyListener>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry unsubscribe " + failed);
                }
                try {
                    for (Map.Entry<SalukiURL, Set<NotifyListener>> entry : failed.entrySet()) {
                        SalukiURL url = entry.getKey();
                        Set<NotifyListener> listeners = entry.getValue();
                        for (NotifyListener listener : listeners) {
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
        if (!failedNotified.isEmpty()) {
            Map<SalukiURL, Map<NotifyListener, List<SalukiURL>>> failed = new HashMap<SalukiURL, Map<NotifyListener, List<SalukiURL>>>(failedNotified);
            for (Map.Entry<SalukiURL, Map<NotifyListener, List<SalukiURL>>> entry : new HashMap<SalukiURL, Map<NotifyListener, List<SalukiURL>>>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry notify " + failed);
                }
                try {
                    for (Map<NotifyListener, List<SalukiURL>> values : failed.values()) {
                        for (Map.Entry<NotifyListener, List<SalukiURL>> entry : values.entrySet()) {
                            try {
                                NotifyListener listener = entry.getKey();
                                List<SalukiURL> urls = entry.getValue();
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
    }

    public Future<?> getRetryFuture() {
        return retryFuture;
    }

    public Set<SalukiURL> getFailedRegistered() {
        return failedRegistered;
    }

    public Set<SalukiURL> getFailedUnregistered() {
        return failedUnregistered;
    }

    public Map<SalukiURL, Set<NotifyListener>> getFailedSubscribed() {
        return failedSubscribed;
    }

    public Map<SalukiURL, Set<NotifyListener>> getFailedUnsubscribed() {
        return failedUnsubscribed;
    }

    public Map<SalukiURL, Map<NotifyListener, List<SalukiURL>>> getFailedNotified() {
        return failedNotified;
    }

    private void addFailedSubscribed(SalukiURL url, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(url);
        if (listeners == null) {
            listeners = Sets.newConcurrentHashSet();
            listeners = failedSubscribed.putIfAbsent(url, listeners);
        }
        listeners.add(listener);
    }

    private void removeFailedSubscribed(SalukiURL url, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        listeners = failedUnsubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        Map<NotifyListener, List<SalukiURL>> notified = failedNotified.get(url);
        if (notified != null) {
            notified.remove(listener);
        }
    }

    @Override
    public void register(SalukiURL url) {
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
    public void unregister(SalukiURL url) {
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
    public void subscribe(SalukiURL url, NotifyListener listener) {
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
    public void unsubscribe(SalukiURL url, NotifyListener listener) {
        super.unsubscribe(url, listener);
        removeFailedSubscribed(url, listener);
        try {
            // 向服务器端发送取消订阅请求
            doUnsubscribe(url, listener);
        } catch (Exception e) {
            logger.error("Failed to unsubscribe " + url + ", waiting for retry, cause: " + e.getMessage(), e);
            // 将失败的取消订阅请求记录到失败列表，定时重试
            Set<NotifyListener> listeners = failedUnsubscribed.get(url);
            if (listeners == null) {
                listeners = Sets.newConcurrentHashSet();
                listeners = failedUnsubscribed.putIfAbsent(url, listeners);
            }
            listeners.add(listener);
        }
    }

    @Override
    protected void notify(SalukiURL url, NotifyListener listener, List<SalukiURL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        try {
            doNotify(url, listener, urls);
        } catch (Exception t) {
            // 将失败的通知请求记录到失败列表，定时重试
            Map<NotifyListener, List<SalukiURL>> listeners = failedNotified.get(url);
            if (listeners == null) {
                failedNotified.putIfAbsent(url, Maps.newConcurrentMap());
                listeners = failedNotified.get(url);
            }
            listeners.put(listener, urls);
            logger.error("Failed to notify for subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
        }
    }

    protected void doNotify(SalukiURL url, NotifyListener listener, List<SalukiURL> urls) {
        super.notify(url, listener, urls);
    }

    @Override
    protected void recover() throws Exception {
        // register
        Set<SalukiURL> recoverRegistered = Sets.newHashSet(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recover register url " + recoverRegistered);
            }
            for (SalukiURL url : recoverRegistered) {
                failedRegistered.add(url);
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
                    addFailedSubscribed(url, listener);
                }
            }
        }
    }

    // ==== 模板方法 ====
    protected abstract void doRegister(SalukiURL url);

    protected abstract void doUnregister(SalukiURL url);

    protected abstract void doSubscribe(SalukiURL url, NotifyListener listener);

    protected abstract void doUnsubscribe(SalukiURL url, NotifyListener listener);

}
