package com.quancheng.saluki.core.common;

import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.StringUtil;

public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolId = new AtomicInteger();

    private final AtomicInteger        nextId = new AtomicInteger();
    private final String               prefix;
    private final boolean              daemon;
    private final int                  priority;
    protected final ThreadGroup        threadGroup;

    public NamedThreadFactory(Class<?> poolType){
        this(poolType, false, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String poolName){
        this(poolName, false, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(Class<?> poolType, boolean daemon){
        this(poolType, daemon, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String poolName, boolean daemon){
        this(poolName, daemon, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(Class<?> poolType, int priority){
        this(poolType, false, priority);
    }

    public NamedThreadFactory(String poolName, int priority){
        this(poolName, false, priority);
    }

    public NamedThreadFactory(Class<?> poolType, boolean daemon, int priority){
        this(toPoolName(poolType), daemon, priority);
    }

    public static String toPoolName(Class<?> poolType) {
        if (poolType == null) {
            throw new NullPointerException("poolType");
        }

        String poolName = StringUtil.simpleClassName(poolType);
        switch (poolName.length()) {
            case 0:
                return "unknown";
            case 1:
                return poolName.toLowerCase(Locale.US);
            default:
                if (Character.isUpperCase(poolName.charAt(0)) && Character.isLowerCase(poolName.charAt(1))) {
                    return Character.toLowerCase(poolName.charAt(0)) + poolName.substring(1);
                } else {
                    return poolName;
                }
        }
    }

    public NamedThreadFactory(String poolName, boolean daemon, int priority, ThreadGroup threadGroup){
        if (poolName == null) {
            throw new NullPointerException("poolName");
        }
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("priority: " + priority
                                               + " (expected: Thread.MIN_PRIORITY <= priority <= Thread.MAX_PRIORITY)");
        }

        prefix = poolName + '-' + poolId.incrementAndGet() + '-';
        this.daemon = daemon;
        this.priority = priority;
        this.threadGroup = threadGroup;
    }

    public NamedThreadFactory(String poolName, boolean daemon, int priority){
        this(poolName, daemon, priority,
             System.getSecurityManager() == null ? Thread.currentThread().getThreadGroup() : System.getSecurityManager().getThreadGroup());
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = newThread(new DefaultRunnableDecorator(r), prefix + nextId.incrementAndGet());
        try {
            if (t.isDaemon()) {
                if (!daemon) {
                    t.setDaemon(false);
                }
            } else {
                if (daemon) {
                    t.setDaemon(true);
                }
            }

            if (t.getPriority() != priority) {
                t.setPriority(priority);
            }
        } catch (Exception ignored) {
            // Doesn't matter even if failed to set.
        }
        return t;
    }

    protected Thread newThread(Runnable r, String name) {
        return new FastThreadLocalThread(threadGroup, r, name);
    }

    private static final class DefaultRunnableDecorator implements Runnable {

        private final Runnable r;

        DefaultRunnableDecorator(Runnable r){
            this.r = r;
        }

        @Override
        public void run() {
            try {
                r.run();
            } finally {
                FastThreadLocal.removeAll();
            }
        }
    }
}
