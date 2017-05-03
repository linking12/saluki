/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.failover;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.quancheng.saluki.core.common.RpcContext;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

/**
 * @author liushiming 2017年5月2日 下午5:42:42
 * @version $Id: RetryCallListener.java, v 0.0.1 2017年5月2日 下午5:42:42 liushiming
 */
public class FailOverListener<Request, Response> extends ClientCall.Listener<Response> implements Runnable {

    private final static Logger              log              = LoggerFactory.getLogger(FailOverListener.class);

    private final ExecutorService            retryExecutor    = Executors.newSingleThreadScheduledExecutor();

    private final CompletionFuture<Response> completionFuture = new CompletionFuture<Response>();

    private static final class CompletionFuture<Response> extends AbstractFuture<Response> {

        @Override
        protected boolean set(Response resp) {
            return super.set(resp);
        }

        @Override
        protected boolean setException(Throwable throwable) {
            return super.setException(throwable);
        }

    }

    private final Integer                             retriesOptions;

    private final Channel                             channel;

    private final MethodDescriptor<Request, Response> method;

    private final Request                             request;

    private final CallOptions                         callOptions;

    private volatile Response                         response;

    public FailOverListener(final Integer retriesOptions, final Request request, final Channel channel,
                            final MethodDescriptor<Request, Response> method, final CallOptions callOptions){
        this.retriesOptions = retriesOptions;
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
        Map<String, Object> affinity = callOptions.getOption(GrpcClientCall.CALLOPTIONS_CUSTOME_KEY);
        try {
            affinity.put(GrpcClientCall.GRPC_CURRENT_ADDR_KEY, currentServer);
        } finally {
            NameResolverNotify notify = new NameResolverNotify(affinity);
            Status.Code code = status.getCode();
            if (code == Status.Code.OK) {
                if (response == null) {
                    completionFuture.setException(Status.UNAVAILABLE.withDescription("No value received for unary call").asRuntimeException());
                }
                if (retries.get() > 0) {
                    notify.resetChannel();
                }
                retries.set(0);
                return;
            } else {
                if (retries.get() >= retriesOptions || retriesOptions == 0) {
                    completionFuture.setException(status.asRuntimeException());
                    notify.resetChannel();
                    return;
                } else {
                    log.error(String.format("Retrying failed call. Failure #%d，Failure Server: %s", retries.get(),
                                            String.valueOf(currentServer)));
                    notify.refreshChannel();
                    retryExecutor.execute(new RetryListenerWrap(this));
                    retries.getAndIncrement();
                }
            }
        }
    }

    private static final class RetryListenerWrap implements Runnable {

        private final static Logger log = LoggerFactory.getLogger(RetryListenerWrap.class);

        private Runnable            listener;

        public RetryListenerWrap(Runnable listener){
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                listener.run();
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    private volatile ClientCall<Request, Response> clientCall;

    @Override
    public void run() {
        ClientCall<Request, Response> clientCallCopy = channel.newCall(method, callOptions);
        RpcContext.getContext().removeAttachment("routerRule");
        try {
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

}
