package com.quancheng.saluki.core.grpc.client.async;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.util.MetadataKeyUtil;

import io.grpc.Attributes.Key;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

public class RetryCallListener<Request, Response> extends ClientCall.Listener<Response> implements Runnable {

    private final static Logger                       log                  = LoggerFactory.getLogger(RetryCallListener.class);

    private final ScheduledExecutorService            retryExecutorService = Executors.newScheduledThreadPool(1);

    private final RetryOptions                        retryOptions;

    private final Channel                             channel;

    private final MethodDescriptor<Request, Response> method;

    private final Request                             request;

    private final CallOptions                         callOptions;

    private volatile Response                         response;

    public RetryCallListener(final RetryOptions retryOptions, final Request request, final Channel channel,
                             final MethodDescriptor<Request, Response> method, final CallOptions callOptions){
        this.retryOptions = retryOptions;
        this.request = request;
        this.channel = channel;
        this.method = method;
        this.callOptions = callOptions;
    }

    @Override
    public void onMessage(Response message) {
        response = message;
        completionFuture.set(response);
    }

    private final AtomicInteger retries = new AtomicInteger(0);

    @Override
    public void onClose(Status status, Metadata trailers) {
        SocketAddress currentServer = getCurrentServer();
        try {
            HashMap<Key<?>, Object> data = Maps.newHashMap();
            data.put(GrpcClientCall.CURRENT_ADDR_KEY, currentServer);
            GrpcClientCall.updateAffinity(callOptions.getAffinity(), data);
        } finally {
            NameResolverNotify notify = new NameResolverNotify(callOptions.getAffinity());
            Status.Code code = status.getCode();
            if (code == Status.Code.OK) {
                if (response == null) {
                    completionFuture.setException(Status.INTERNAL.withDescription("No value received for unary call").asRuntimeException());
                }
                if (retries.get() > 0) {
                    notify.resetChannel();
                }
                retries.set(0);
                return;
            } else {
                if (retries.get() > retryOptions.getReties() || !retryOptions.isEnableRetry()) {
                    String errorCause = trailers.get(MetadataKeyUtil.GRPC_ERRORCAUSE_VALUE);
                    Exception serverException = status.withDescription(errorCause).asRuntimeException();
                    completionFuture.setException(serverException);
                    notify.resetChannel();
                    return;
                } else {
                    log.error(String.format("Retrying failed call. Failure #%d，Current Server is %s", retries.get(),
                                            String.valueOf(currentServer)),
                              status.getCause());
                    clientCall = null;
                    notify.refreshChannel();
                    retryExecutorService.schedule(new RetryListenerWrap(this), retryOptions.nextBackoffMillis(),
                                                  TimeUnit.MILLISECONDS);
                    retries.getAndIncrement();
                }
            }
        }
    }

    private volatile CompletionFuture<Request, Response> completionFuture;

    private volatile ClientCall<Request, Response>       clientCall;

    @Override
    public void run() {
        ClientCall<Request, Response> clientCallCopy = channel.newCall(method, callOptions);
        RpcContext.getContext().removeAttachment("routerRule");
        try {
            completionFuture = new CompletionFuture<Request, Response>(clientCall);
            clientCallCopy.start(this, new Metadata());
            clientCallCopy.request(1);
            try {
                clientCallCopy.sendMessage(request);
            } catch (Throwable t) {
                clientCallCopy.cancel("Exception in sendMessage.", t);
                throw t;
            }
            try {
                clientCallCopy.halfClose();
            } catch (Throwable t) {
                clientCallCopy.cancel("Exception in halfClose.", t);
                throw t;
            }
        } finally {
            clientCall = clientCallCopy;
        }
    }

    public ListenableFuture<Response> getCompletionFuture() {
        return completionFuture;
    }

    public void cancel() {
        if (clientCall != null) {
            clientCall.cancel("User requested cancelation.", null);
        }
    }

    private SocketAddress getCurrentServer() {
        SocketAddress currentServer = clientCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        return currentServer;
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
