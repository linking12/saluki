package com.quancheng.saluki.core.grpc.cluster.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.quancheng.saluki.core.grpc.cluster.metrics.ClusterClientMetrics;
import com.quancheng.saluki.core.grpc.cluster.metrics.ClusterClientMetrics.MetricLevel;
import com.quancheng.saluki.core.grpc.cluster.metrics.Counter;
import com.quancheng.saluki.core.grpc.cluster.metrics.Meter;
import com.quancheng.saluki.core.grpc.cluster.metrics.Timer;
import com.quancheng.saluki.core.grpc.cluster.metrics.Timer.Context;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptors.CheckedForwardingClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

public class ChannelPool extends ManagedChannel {

    protected static final Logger      LOG                = LoggerFactory.getLogger(ChannelPool.class);

    private static final AtomicInteger ChannelIdGenerator = new AtomicInteger();

    protected static Stats             STATS;

    public interface ChannelFactory {

        ManagedChannel create() throws IOException;
    }

    private static class Stats {

        Counter ACTIVE_CHANNEL_COUNTER = ClusterClientMetrics.counter(MetricLevel.Info, "grpc.channel.active");

        Counter ACTIVE_RPC_COUNTER     = ClusterClientMetrics.counter(MetricLevel.Info, "grpc.rpc.active");

        Meter   RPC_METER              = ClusterClientMetrics.meter(MetricLevel.Info, "grpc.rpc.performed");
    }

    protected static synchronized Stats getStats() {
        if (STATS == null) {
            STATS = new Stats();
        }
        return STATS;
    }

    private class InstrumentedChannel extends ManagedChannel {

        private final ManagedChannel delegate;
        private final Timer          timer;
        private final AtomicBoolean  active = new AtomicBoolean(true);

        public InstrumentedChannel(ManagedChannel channel){
            this.delegate = channel;
            this.timer = ClusterClientMetrics.timer(MetricLevel.Trace,
                                                     "channels.channel" + ChannelIdGenerator.incrementAndGet()
                                                                        + ".rpc.latency");
            getStats().ACTIVE_CHANNEL_COUNTER.inc();
        }

        private synchronized void markInactive() {
            boolean previouslyActive = active.getAndSet(false);
            if (previouslyActive) {
                getStats().ACTIVE_CHANNEL_COUNTER.dec();
            }
        }

        @Override
        public ManagedChannel shutdown() {
            markInactive();
            return delegate.shutdown();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public ManagedChannel shutdownNow() {
            markInactive();
            return delegate.shutdownNow();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            markInactive();
            return delegate.awaitTermination(timeout, unit);
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> newCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                             CallOptions callOptions) {
            final Context timerContext = timer.time();
            final AtomicBoolean decremented = new AtomicBoolean(false);
            return new CheckedForwardingClientCall<ReqT, RespT>(delegate.newCall(methodDescriptor, callOptions)) {

                @Override
                protected void checkedStart(ClientCall.Listener<RespT> responseListener,
                                            Metadata headers) throws Exception {
                    for (HeaderInterceptor interceptor : headerInterceptors) {
                        interceptor.updateHeaders(headers);
                    }
                    ClientCall.Listener<RespT> timingListener = wrap(responseListener, timerContext, decremented);
                    getStats().ACTIVE_RPC_COUNTER.inc();
                    getStats().RPC_METER.mark();
                    delegate().start(timingListener, headers);
                }

                @Override
                public void cancel(String message, Throwable cause) {
                    if (!decremented.getAndSet(true)) {
                        getStats().ACTIVE_RPC_COUNTER.dec();
                    }
                    super.cancel(message, cause);
                }
            };
        }

        protected <RespT> ClientCall.Listener<RespT> wrap(final ClientCall.Listener<RespT> delegate,
                                                          final Context timeContext, final AtomicBoolean decremented) {
            return new ClientCall.Listener<RespT>() {

                @Override
                public void onHeaders(Metadata headers) {
                    delegate.onHeaders(headers);
                }

                @Override
                public void onMessage(RespT message) {
                    delegate.onMessage(message);
                }

                @Override
                public void onClose(Status status, Metadata trailers) {
                    try {
                        if (!decremented.getAndSet(true)) {
                            getStats().ACTIVE_RPC_COUNTER.dec();
                        }
                        if (!status.isOk()) {
                            ClusterClientMetrics.meter(MetricLevel.Info,
                                                        "grpc.errors." + status.getCode().name()).mark();
                        }
                        delegate.onClose(status, trailers);
                    } finally {
                        timeContext.close();
                    }
                }

                @Override
                public void onReady() {
                    delegate.onReady();
                }
            };
        }

        @Override
        public String authority() {
            return delegate.authority();
        }
    }

    private final AtomicReference<ImmutableList<InstrumentedChannel>> channels     = new AtomicReference<>();
    private final AtomicInteger                                       requestCount = new AtomicInteger();
    private final ImmutableList<HeaderInterceptor>                    headerInterceptors;
    private final ChannelFactory                                      factory;
    private final String                                              authority;

    private boolean                                                   shutdown     = false;

    public ChannelPool(List<HeaderInterceptor> headerInterceptors, ChannelFactory factory) throws IOException{
        this.factory = factory;
        InstrumentedChannel channel = new InstrumentedChannel(factory.create());
        this.channels.set(ImmutableList.of(channel));
        authority = channel.authority();
        if (headerInterceptors == null) {
            this.headerInterceptors = ImmutableList.of();
        } else {
            this.headerInterceptors = ImmutableList.copyOf(headerInterceptors);
        }
    }

    public void ensureChannelCount(int capacity) throws IOException {
        if (this.shutdown) {
            throw new IOException("The channel is closed.");
        }
        if (channels.get().size() < capacity) {
            synchronized (this) {
                if (channels.get().size() < capacity) {
                    List<InstrumentedChannel> newChannelList = new ArrayList<>(channels.get());
                    while (newChannelList.size() < capacity) {
                        newChannelList.add(new InstrumentedChannel(factory.create()));
                    }
                    setChannels(newChannelList);
                }
            }
        }
    }

    private InstrumentedChannel getNextChannel() {
        int currentRequestNum = requestCount.getAndIncrement();
        ImmutableList<InstrumentedChannel> channelsList = channels.get();
        int index = Math.abs(currentRequestNum % channelsList.size());
        return channelsList.get(index);
    }

    @Override
    public String authority() {
        return authority;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> newCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                         CallOptions callOptions) {
        Preconditions.checkState(!shutdown, "Cannot perform operations on a closed connection");
        return getNextChannel().newCall(methodDescriptor, callOptions);
    }

    private void setChannels(List<InstrumentedChannel> newChannelList) {
        channels.set(ImmutableList.copyOf(newChannelList));
    }

    public int size() {
        return channels.get().size();
    }

    @Override
    public synchronized ManagedChannel shutdown() {
        for (InstrumentedChannel channelWrapper : channels.get()) {
            channelWrapper.shutdown();
        }
        this.shutdown = true;
        return this;
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        for (InstrumentedChannel channel : channels.get()) {
            if (!channel.isTerminated()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ManagedChannel shutdownNow() {
        for (InstrumentedChannel channel : channels.get()) {
            if (!channel.isTerminated()) {
                channel.shutdownNow();
            }
        }
        return this;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long endTimeNanos = System.nanoTime() + unit.toNanos(timeout);
        for (InstrumentedChannel channel : channels.get()) {
            if (channel.isTerminated()) {
                continue;
            }
            long awaitTimeNanos = endTimeNanos - System.nanoTime();
            if (awaitTimeNanos <= 0) {
                break;
            }
            channel.awaitTermination(awaitTimeNanos, TimeUnit.NANOSECONDS);
        }

        return isTerminated();
    }
}
