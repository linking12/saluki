package com.quancheng.saluki.core.grpc.client.ha;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.Attributes;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public abstract class AbstractRetryingRpcListener<RequestT, ResponseT, ResultT> extends ClientCall.Listener<ResponseT> implements Runnable {

    protected final static Logger                     LOG              = LoggerFactory.getLogger(AbstractRetryingRpcListener.class);
    private final RetryOptions                        retryOptions;
    private final SalukiAsyncRpc<RequestT, ResponseT> rpc;
    private final RequestT                            request;
    private final CallOptions                         callOptions;
    private final ScheduledExecutorService            retryExecutorService;
    private int                                       retryCount;
    private final Metadata                            originalMetadata;
    protected final GrpcFuture<ResultT>               completionFuture = new GrpcFuture<>();
    protected ClientCall<RequestT, ResponseT>         call;

    public AbstractRetryingRpcListener(RetryOptions retryOptions, RequestT request,
                                       SalukiAsyncRpc<RequestT, ResponseT> retryableRpc, CallOptions callOptions,
                                       ScheduledExecutorService retryExecutorService, Metadata originalMetadata){
        this.retryOptions = retryOptions;
        this.request = request;
        this.rpc = retryableRpc;
        this.callOptions = callOptions;
        this.retryExecutorService = retryExecutorService;
        this.originalMetadata = originalMetadata;
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
        Status.Code code = status.getCode();
        if (code == Status.Code.OK) {
            onOK();
            return;
        } else {
            if (retryCount >= retryOptions.getReties()) {
                String message = String.format("Exhausted retries after %d failures.", retryCount);
                StatusRuntimeException cause = status.asRuntimeException();
                completionFuture.setException(new RetriesExhaustedException(message, cause));
                return;
            } else if (!retryOptions.isEnableRetry()) {
                completionFuture.setException(status.asRuntimeException());
                return;
            } else {
                // Retry
                LOG.info("Retrying failed call. Failure #%d, got: %s", status.getCause(), retryCount, status);
                call = null;
                pickNormalSock();
                retryExecutorService.schedule(this, retryOptions.nextBackoffMillis(), TimeUnit.MILLISECONDS);
                retryCount += 1;
            }
        }
    }

    protected abstract void onOK();

    private void pickNormalSock() {
        Attributes affinity = callOptions.getAffinity();
        SocketAddress currentServer = affinity.get(CallOptionsFactory.REMOTE_ADDR_KEY);
        List<SocketAddress> servers = affinity.get(CallOptionsFactory.REMOTE_ADDR_KEYS);
        NameResolver.Listener listener = affinity.get(CallOptionsFactory.NAMERESOVER_LISTENER);
        List<SocketAddress> serversCopy = Lists.newArrayList();
        if (listener != null && currentServer != null && servers != null) {
            InetSocketAddress currentSock = (InetSocketAddress) currentServer;
            for (int i = 0; i < servers.size(); i++) {
                InetSocketAddress inetSock = (InetSocketAddress) servers.get(i);
                if (!inetSock.getHostName().equals(currentSock.getHostName())) {
                    serversCopy.add(inetSock);
                }
            }
            notifyChannel(listener, serversCopy);
        }
    }

    private void notifyChannel(NameResolver.Listener listener, List<SocketAddress> servers) {
        List<ResolvedServerInfo> resolvedServers = new ArrayList<ResolvedServerInfo>(servers.size());
        Attributes config = Attributes.newBuilder().set(CallOptionsFactory.NAMERESOVER_LISTENER, listener).build();
        for (SocketAddress sock : servers) {
            ResolvedServerInfo serverInfo = new ResolvedServerInfo(sock, config);
            resolvedServers.add(serverInfo);
        }
        listener.onUpdate(Collections.singletonList(resolvedServers), config);
    }

    public ListenableFuture<ResultT> getCompletionFuture() {
        return completionFuture;
    }

    @Override
    public void run() {
        Metadata metadata = new Metadata();
        metadata.merge(originalMetadata);
        this.call = rpc.newCall(callOptions);
        rpc.start(this.call, getRetryRequest(), this, metadata);
    }

    protected RequestT getRetryRequest() {
        return request;
    }

    public void start() {
        run();
    }

    public void cancel() {
        if (this.call != null) {
            call.cancel("User requested cancelation.", null);
        }
    }

    protected class GrpcFuture<RespT> extends AbstractFuture<RespT> {

        @Override
        protected void interruptTask() {
            if (call != null) {
                call.cancel("Request interrupted.", null);
            }
        }

        @Override
        protected boolean set(@Nullable RespT resp) {
            return super.set(resp);
        }

        @Override
        protected boolean setException(Throwable throwable) {
            return super.setException(throwable);
        }
    }
}
