package com.quancheng.saluki.monitor.web.utils;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamedThreadFactory implements ThreadFactory {

    protected final Logger             logger     = LoggerFactory.getLogger(getClass());

    private static final AtomicInteger POOL_SEQ   = new AtomicInteger(1);

    private final AtomicInteger        mThreadNum = new AtomicInteger(1);

    private final String               mPrefix;

    private final boolean              mDaemo;

    private final ThreadGroup          mGroup;

    public NamedThreadFactory(){
        this("pool-" + POOL_SEQ.getAndIncrement(), false);
    }

    public NamedThreadFactory(String prefix){
        this(prefix, false);
    }

    public NamedThreadFactory(String prefix, boolean daemo){
        mPrefix = prefix + "-thread-";
        mDaemo = daemo;
        SecurityManager s = System.getSecurityManager();
        mGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    public Thread newThread(Runnable runnable) {
        String name = mPrefix + mThreadNum.getAndIncrement();
        Thread ret = new Thread(mGroup, runnable, name, 0);
        ret.setDaemon(mDaemo);
        ret.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.error(t.getName() + " excutor failed", e);
            }
        });
        return ret;
    }

    public ThreadGroup getThreadGroup() {
        return mGroup;
    }
}
