package com.quancheng.saluki.core.grpc.client.async;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.quancheng.saluki.core.grpc.client.GrpcAsyncCall;
import com.quancheng.saluki.core.grpc.client.async.AsyncCallInternal.AsyncCallClientInternal;
import com.quancheng.saluki.core.grpc.util.MetadataKeyUtil;

import io.grpc.Attributes.Key;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.Status;

public class RetryCallListener<Request, Response> extends ClientCall.Listener<Response> implements Runnable {

    private final static Logger                              log                  = LoggerFactory.getLogger(RetryCallListener.class);

    private final ScheduledExecutorService                   retryExecutorService = Executors.newScheduledThreadPool(1);

    private final RetryOptions                               retryOptions;

    private final AsyncCallClientInternal<Request, Response> rpc;

    private final Request                                    request;

    private final CallOptions                                callOptions;

    private CompletionFuture<Request, Response>              completionFuture;

    private ClientCall<Request, Response>                    clientCall;

    private int                                              retryCount;

    private Response                                         response;

    public RetryCallListener(RetryOptions retryOptions, Request request,
                             AsyncCallClientInternal<Request, Response> retryableRpc, CallOptions callOptions){
        this.retryOptions = retryOptions;
        this.request = request;
        this.rpc = retryableRpc;
        this.callOptions = callOptions;
    }

    @Override
    public void onMessage(Response message) {
        response = message;
        completionFuture.set(response);
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
        try {
            cacheCurrentServer();
        } finally {
            NameResolverNotify notify = new NameResolverNotify(callOptions.getAffinity());
            Status.Code code = status.getCode();
            if (code == Status.Code.OK) {
                if (response == null) {
                    completionFuture.setException(Status.INTERNAL.withDescription("No value received for unary call").asRuntimeException());
                }
                if (retryCount > 0) {
                    notify.resetChannel();
                }
                return;
            } else {
                if (retryCount > retryOptions.getReties() || !retryOptions.isEnableRetry()) {
                    String errorCause = trailers.get(MetadataKeyUtil.GRPC_ERRORCAUSE_VALUE);
                    Exception serverException = status.withDescription(errorCause).asRuntimeException();
                    completionFuture.setException(serverException);
                    notify.resetChannel();
                    return;
                } else {
                    log.error(String.format("Retrying failed call. Failure #%d", retryCount), status.getCause());
                    clientCall = null;
                    notify.refreshChannel();
                    retryExecutorService.schedule(new RetryListenerWrap(this), retryOptions.nextBackoffMillis(),
                                                  TimeUnit.MILLISECONDS);
                    retryCount += 1;
                }
            }
        }
    }

    @Override
    public void run() {
        clientCall = rpc.newCall(callOptions);
        completionFuture = new CompletionFuture<Request, Response>(clientCall);
        rpc.start(this.clientCall, this.request, this, new Metadata());
    }

    public ListenableFuture<Response> getCompletionFuture() {
        return completionFuture;
    }

    public void cancel() {
        if (this.clientCall != null) {
            clientCall.cancel("User requested cancelation.", null);
        }
    }

    private void cacheCurrentServer() {
        SocketAddress currentServer = clientCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        HashMap<Key<?>, Object> data = Maps.newHashMap();
        data.put(GrpcAsyncCall.CURRENT_ADDR_KEY, currentServer);
        GrpcAsyncCall.updateAffinity(callOptions.getAffinity(), data);
    }

    private static final class CompletionFuture<Request, Response> extends AbstractFuture<Response> {

        protected ClientCall<Request, Response> clientCall;

        public CompletionFuture(ClientCall<Request, Response> call){
            this.clientCall = call;
        }

        @Override
        protected void interruptTask() {
            if (clientCall != null) {
                clientCall.cancel("Request interrupted.", null);
            }
        }

        @Override
        protected boolean set(@Nullable Response resp) {
            return super.set(resp);
        }

        @Override
        protected boolean setException(Throwable throwable) {
            throwable.setStackTrace(new StackTraceElement[] {});
            return super.setException(throwable);
        }
    }

}
