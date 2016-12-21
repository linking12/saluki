/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.ypp.thrall.core.grpc.client;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Predicates;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;
import com.ypp.thrall.core.grpc.client.async.AbstractRetryingRpcListener;
import com.ypp.thrall.core.grpc.client.async.AsyncCallInternal;
import com.ypp.thrall.core.grpc.client.async.AsyncCallInternal.AsyncCallClientInternal;
import com.ypp.thrall.core.grpc.client.async.RetryOptions;
import com.ypp.thrall.core.grpc.client.async.RetryingUnaryRpcCallListener;

import io.grpc.Attributes;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.NameResolver;
import io.grpc.Status;

/**
 * @author shimingliu 2016年12月14日 下午9:54:44
 * @version GrpcAsyncCall.java, v 0.0.1 2016年12月14日 下午9:54:44 shimingliu
 */
public interface GrpcAsyncCall {

    public static final Attributes.Key<SocketAddress>         REMOTE_ADDR_KEY           = Attributes.Key.of("remote-addr");

    public static final Attributes.Key<List<SocketAddress>>   REMOTE_ADDR_KEYS          = Attributes.Key.of("remote-addrs");

    public static final Attributes.Key<List<SocketAddress>>   REMOTE_ADDR_KEYS_REGISTRY = Attributes.Key.of("remote-addrs-registry");

    public static final Attributes.Key<NameResolver.Listener> NAMERESOVER_LISTENER      = Attributes.Key.of("nameResolver-Listener");

    public ListenableFuture<Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method);

    public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method);

    public SocketAddress getRemoteAddress();

    public static GrpcAsyncCall createGrpcAsyncCall(final Channel channel, final RetryOptions retryOptions) {
        return new GrpcAsyncCall() {

            private volatile CallOptions callOptions;

            @Override
            public ListenableFuture<Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method) {
                return getCompletionFuture(createUnaryListener(request, buildAsyncRpc(method)));
            }

            @Override
            public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method) {
                return getBlockingResult(createUnaryListener(request, buildAsyncRpc(method)));
            }

            @Override
            public SocketAddress getRemoteAddress() {
                return callOptions.getAffinity().get(REMOTE_ADDR_KEY);
            }

            /**
             * Help Method
             */
            private AsyncCallClientInternal<Message, Message> buildAsyncRpc(MethodDescriptor<Message, Message> method) {
                AsyncCallClientInternal<Message, Message> asyncRpc = AsyncCallInternal.createGrpcAsyncCall(channel,
                                                                                                           method,
                                                                                                           Predicates.<Message> alwaysTrue());
                return asyncRpc;
            }

            private <ReqT, RespT> RetryingUnaryRpcCallListener<ReqT, RespT> createUnaryListener(ReqT request,
                                                                                                AsyncCallClientInternal<ReqT, RespT> rpc) {
                CallOptions callOptions = getCallOptions(rpc.getMethodDescriptor(), request);
                return new RetryingUnaryRpcCallListener<>(retryOptions, request, rpc, callOptions, new Metadata());
            }

            private <ReqT> CallOptions getCallOptions(final MethodDescriptor<ReqT, ?> methodDescriptor, ReqT request) {
                CallOptions callOptions = AsyncCallInternal.createCallOptions(methodDescriptor, request);
                this.callOptions = callOptions;
                return callOptions;
            }

            private <ReqT, RespT, OutputT> ListenableFuture<OutputT> getCompletionFuture(AbstractRetryingRpcListener<ReqT, RespT, OutputT> listener) {
                listener.start();
                return listener.getCompletionFuture();
            }

            private <ReqT, RespT, OutputT> OutputT getBlockingResult(AbstractRetryingRpcListener<ReqT, RespT, OutputT> listener) {
                try {
                    listener.start();
                    return listener.getCompletionFuture().get();
                } catch (InterruptedException e) {
                    listener.cancel();
                    throw Status.CANCELLED.withCause(e).asRuntimeException();
                } catch (ExecutionException e) {
                    listener.cancel();
                    throw Status.fromThrowable(e).asRuntimeException();
                }
            }
        };
    }

}
