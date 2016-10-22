package com.quancheng.saluki.core.grpc.ha;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.api.client.util.BackOff;
import com.google.api.client.util.ExponentialBackOff;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

import io.grpc.Status;
import io.grpc.Status.Code;

public class RetryOptions implements Serializable {

    private static final long                     serialVersionUID                    = 1L;

    public static int                             DEFAULT_STREAMING_BUFFER_SIZE       = 60;

    public static final boolean                   DEFAULT_ENABLE_GRPC_RETRIES         = true;

    public static final ImmutableSet<Status.Code> DEFAULT_ENABLE_GRPC_RETRIES_SET     = ImmutableSet.of(Status.Code.DEADLINE_EXCEEDED,
                                                                                                        Status.Code.INTERNAL,
                                                                                                        Status.Code.UNAVAILABLE,
                                                                                                        Status.Code.ABORTED);

    public static final int                       DEFAULT_READ_PARTIAL_ROW_TIMEOUT_MS = (int) TimeUnit.MILLISECONDS.convert(60,
                                                                                                                            TimeUnit.SECONDS);

    public static final int                       DEFAULT_INITIAL_BACKOFF_MILLIS      = 5;

    public static final double                    DEFAULT_BACKOFF_MULTIPLIER          = 2;

    public static final int                       DEFAULT_MAX_ELAPSED_BACKOFF_MILLIS  = (int) TimeUnit.MILLISECONDS.convert(60,
                                                                                                                            TimeUnit.SECONDS);

    public static final int                       DEFAULT_MAX_SCAN_TIMEOUT_RETRIES    = 3;

    public static class Builder {

        private boolean          enableRetries               = DEFAULT_ENABLE_GRPC_RETRIES;
        private int              initialBackoffMillis        = DEFAULT_INITIAL_BACKOFF_MILLIS;
        private double           backoffMultiplier           = DEFAULT_BACKOFF_MULTIPLIER;
        private int              maxElaspedBackoffMillis     = DEFAULT_MAX_ELAPSED_BACKOFF_MILLIS;
        private int              streamingBufferSize         = DEFAULT_STREAMING_BUFFER_SIZE;
        private int              readPartialRowTimeoutMillis = DEFAULT_READ_PARTIAL_ROW_TIMEOUT_MS;
        private int              maxScanTimeoutRetries       = DEFAULT_MAX_SCAN_TIMEOUT_RETRIES;
        private Set<Status.Code> statusToRetryOn             = new HashSet<>(DEFAULT_ENABLE_GRPC_RETRIES_SET);
        private boolean          allowRetriesWithoutTimestamp;

        public Builder(){
        }

        public Builder(RetryOptions options){
            this.enableRetries = options.retriesEnabled;
            this.initialBackoffMillis = options.initialBackoffMillis;
            this.backoffMultiplier = options.backoffMultiplier;
            this.maxElaspedBackoffMillis = options.maxElaspedBackoffMillis;
            this.streamingBufferSize = options.streamingBufferSize;
            this.readPartialRowTimeoutMillis = options.readPartialRowTimeoutMillis;
            this.maxScanTimeoutRetries = options.maxScanTimeoutRetries;
            this.statusToRetryOn = new HashSet<>(options.statusToRetryOn);
            this.allowRetriesWithoutTimestamp = options.allowRetriesWithoutTimestamp;
        }

        public Builder setEnableRetries(boolean enabled) {
            this.enableRetries = enabled;
            return this;
        }

        public Builder setRetryOnDeadlineExceeded(boolean enabled) {
            if (enabled) {
                statusToRetryOn.add(Status.Code.DEADLINE_EXCEEDED);
            } else {
                statusToRetryOn.remove(Status.Code.DEADLINE_EXCEEDED);
            }
            return this;
        }

        public Builder setInitialBackoffMillis(int initialBackoffMillis) {
            this.initialBackoffMillis = initialBackoffMillis;
            return this;
        }

        public Builder setBackoffMultiplier(double multiplier) {
            this.backoffMultiplier = multiplier;
            return this;
        }

        public Builder setMaxElapsedBackoffMillis(int maxElaspedBackoffMillis) {
            this.maxElaspedBackoffMillis = maxElaspedBackoffMillis;
            return this;
        }

        public Builder setStreamingBufferSize(int streamingBufferSize) {
            this.streamingBufferSize = streamingBufferSize;
            return this;
        }

        public Builder setReadPartialRowTimeoutMillis(int timeout) {
            this.readPartialRowTimeoutMillis = timeout;
            return this;
        }

        public Builder setMaxScanTimeoutRetries(int maxScanTimeoutRetries) {
            this.maxScanTimeoutRetries = maxScanTimeoutRetries;
            return this;
        }

        public Builder addStatusToRetryOn(Status.Code code) {
            statusToRetryOn.add(code);
            return this;
        }

        public Builder setAllowRetriesWithoutTimestamp(boolean allowRetriesWithoutTimestamp) {
            this.allowRetriesWithoutTimestamp = allowRetriesWithoutTimestamp;
            return this;
        }

        public RetryOptions build() {
            return new RetryOptions(enableRetries, allowRetriesWithoutTimestamp, initialBackoffMillis,
                                    backoffMultiplier, maxElaspedBackoffMillis, streamingBufferSize,
                                    readPartialRowTimeoutMillis, maxScanTimeoutRetries,
                                    ImmutableSet.copyOf(statusToRetryOn));
        }
    }

    private final boolean            retriesEnabled;
    private final boolean            allowRetriesWithoutTimestamp;
    private final int                initialBackoffMillis;
    private final int                maxElaspedBackoffMillis;
    private final double             backoffMultiplier;
    private final int                streamingBufferSize;
    private final int                readPartialRowTimeoutMillis;
    private final int                maxScanTimeoutRetries;
    private final ImmutableSet<Code> statusToRetryOn;

    public RetryOptions(boolean retriesEnabled, boolean allowRetriesWithoutTimestamp, int initialBackoffMillis,
                        double backoffMultiplier, int maxElaspedBackoffMillis, int streamingBufferSize,
                        int readPartialRowTimeoutMillis, int maxScanTimeoutRetries, ImmutableSet<Code> statusToRetryOn){
        this.retriesEnabled = retriesEnabled;
        this.allowRetriesWithoutTimestamp = allowRetriesWithoutTimestamp;
        this.initialBackoffMillis = initialBackoffMillis;
        this.maxElaspedBackoffMillis = maxElaspedBackoffMillis;
        this.backoffMultiplier = backoffMultiplier;
        this.streamingBufferSize = streamingBufferSize;
        this.readPartialRowTimeoutMillis = readPartialRowTimeoutMillis;
        this.maxScanTimeoutRetries = maxScanTimeoutRetries;
        this.statusToRetryOn = statusToRetryOn;
    }

    public int getInitialBackoffMillis() {
        return initialBackoffMillis;
    }

    public int getMaxElaspedBackoffMillis() {
        return maxElaspedBackoffMillis;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public boolean enableRetries() {
        return retriesEnabled;
    }

    public boolean allowRetriesWithoutTimestamp() {
        return allowRetriesWithoutTimestamp;
    }

    public boolean retryOnDeadlineExceeded() {
        return statusToRetryOn.contains(Status.Code.DEADLINE_EXCEEDED);
    }

    public int getStreamingBufferSize() {
        return streamingBufferSize;
    }

    public int getReadPartialRowTimeoutMillis() {
        return readPartialRowTimeoutMillis;
    }

    public int getMaxScanTimeoutRetries() {
        return maxScanTimeoutRetries;
    }

    public boolean isRetryable(Status.Code code) {
        return statusToRetryOn.contains(code);
    }

    public BackOff createBackoff() {
        return createBackoffBuilder().build();
    }

    @VisibleForTesting
    protected ExponentialBackOff.Builder createBackoffBuilder() {
        return new ExponentialBackOff.Builder().setInitialIntervalMillis(getInitialBackoffMillis()).setMaxElapsedTimeMillis(getMaxElaspedBackoffMillis()).setMultiplier(getBackoffMultiplier());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != RetryOptions.class) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        RetryOptions other = (RetryOptions) obj;

        return retriesEnabled == other.retriesEnabled
               && allowRetriesWithoutTimestamp == other.allowRetriesWithoutTimestamp
               && Objects.equals(statusToRetryOn, other.statusToRetryOn)
               && initialBackoffMillis == other.initialBackoffMillis
               && maxElaspedBackoffMillis == other.maxElaspedBackoffMillis
               && backoffMultiplier == other.backoffMultiplier && streamingBufferSize == other.streamingBufferSize
               && readPartialRowTimeoutMillis == other.readPartialRowTimeoutMillis
               && maxScanTimeoutRetries == other.maxScanTimeoutRetries;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues().add("retriesEnabled",
                                                                     retriesEnabled).add("allowRetriesWithoutTimestamp",
                                                                                         allowRetriesWithoutTimestamp).add("statusToRetryOn",
                                                                                                                           statusToRetryOn).add("initialBackoffMillis",
                                                                                                                                                initialBackoffMillis).add("maxElaspedBackoffMillis",
                                                                                                                                                                          maxElaspedBackoffMillis).add("backoffMultiplier",
                                                                                                                                                                                                       backoffMultiplier).add("streamingBufferSize",
                                                                                                                                                                                                                              streamingBufferSize).add("readPartialRowTimeoutMillis",
                                                                                                                                                                                                                                                       readPartialRowTimeoutMillis).add("maxScanTimeoutRetries",
                                                                                                                                                                                                                                                                                        maxScanTimeoutRetries).toString();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }
}
