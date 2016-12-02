package com.quancheng.saluki.core.grpc.client.async;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RetryOptions implements Serializable {

    private static final long serialVersionUID     = 1L;

    private final int         reties;

    private final boolean     enableRetry;

    private final Random      random               = new Random();

    private double            multiplier           = 1.6;

    private double            jitter               = .2;

    private long              initialBackoffMillis = TimeUnit.SECONDS.toMillis(1);

    private long              nextBackoffMillis    = initialBackoffMillis;

    private long              maxBackoffMillis     = TimeUnit.MINUTES.toMillis(2);

    public RetryOptions(int reties, boolean enableRetry){
        super();
        this.reties = reties;
        this.enableRetry = enableRetry;
    }

    public long getInitialBackoffMillis() {
        return initialBackoffMillis;
    }

    public void setInitialBackoffMillis(long initialBackoffMillis) {
        this.initialBackoffMillis = initialBackoffMillis;
    }

    public long getMaxBackoffMillis() {
        return maxBackoffMillis;
    }

    public void setMaxBackoffMillis(long maxBackoffMillis) {
        this.maxBackoffMillis = maxBackoffMillis;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getJitter() {
        return jitter;
    }

    public void setJitter(double jitter) {
        this.jitter = jitter;
    }

    public int getReties() {
        return reties;
    }

    public boolean isEnableRetry() {
        return enableRetry;
    }

    public long nextBackoffMillis() {
        long currentBackoffMillis = nextBackoffMillis;
        nextBackoffMillis = Math.min((long) (currentBackoffMillis * multiplier), maxBackoffMillis);
        return currentBackoffMillis + uniformRandom(-jitter * currentBackoffMillis, jitter * currentBackoffMillis);
    }

    private long uniformRandom(double low, double high) {
        checkArgument(high >= low);
        double mag = high - low;
        return (long) (random.nextDouble() * mag + low);
    }

}
