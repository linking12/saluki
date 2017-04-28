/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client;

import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.client.async.AbstractRetryingRpcListener;
import com.quancheng.saluki.core.grpc.client.async.AsyncCallInternal;
import com.quancheng.saluki.core.grpc.client.async.AsyncCallInternal.AsyncCallClientInternal;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.grpc.client.async.RetryOptions;
import com.quancheng.saluki.core.grpc.client.async.RetryingUnaryRpcCallListener;

import io.grpc.Attributes;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.Attributes.Key;

/**
 * @author shimingliu 2016年12月14日 下午9:54:44
 * @version GrpcAsyncCall.java, v 0.0.1 2016年12月14日 下午9:54:44 shimingliu
 */
public interface GrpcAsyncCall {

    public static final Attributes.Key<GrpcURL>               GRPC_REF_URL                  = Attributes.Key.of("grpc-refurl");

    public static final Attributes.Key<SocketAddress>         CURRENT_ADDR_KEY              = Attributes.Key.of("current-address");

    public static final Attributes.Key<List<SocketAddress>>   ROUNDROBINED_REMOTE_ADDR_KEYS = Attributes.Key.of("roundrobined-remote-addresss");

    public static final Attributes.Key<List<SocketAddress>>   REGISTRY_REMOTE_ADDR_KEYS     = Attributes.Key.of("registry-remote-addresss");

    public static final Attributes.Key<NameResolver.Listener> NAMERESOVER_LISTENER          = Attributes.Key.of("nameResolver-Listener");

    public ListenableFuture<Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method);

    public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method);

    public SocketAddress getRemoteAddress();

    public static void updateAffinity(Attributes affinity, HashMap<Key<?>, Object> toAddData) {
        HashMap<Key<?>, Object> data = Maps.newHashMap();
        for (Key<?> key : affinity.keys()) {
            Object obj = affinity.get(key);
            data.put(key, obj);
        }
        data.putAll(toAddData);
        try {
            Class<?> classType = affinity.getClass();
            Field[] fields = classType.getDeclaredFields();
            for (Field field : fields) {
                if (HashMap.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    field.set(affinity, data);
                    break;
                }
            }
        } catch (Exception e) {
            RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
            throw rpcFramwork;
        }
    }

    public static GrpcAsyncCall createGrpcAsyncCall(final Channel channel, final RetryOptions retryOptions,
                                                    final Attributes atributes) {
        CallOptions callOptions = CallOptions.DEFAULT.withAffinity(atributes);
        return new GrpcAsyncCall() {

            @Override
            public SocketAddress getRemoteAddress() {
                return callOptions.getAffinity().get(GrpcAsyncCall.CURRENT_ADDR_KEY);
            }

            @Override
            public ListenableFuture<Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method) {
                return getCompletionFuture(createUnaryListener(request, buildAsyncRpc(method)));
            }

            @Override
            public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method) {
                return getBlockingResult(createUnaryListener(request, buildAsyncRpc(method)));
            }

            /**
             * Help Method
             */
            private AsyncCallClientInternal<Message, Message> buildAsyncRpc(MethodDescriptor<Message, Message> method) {
                AsyncCallClientInternal<Message, Message> asyncRpc = AsyncCallInternal.createGrpcAsyncCall(channel,
                                                                                                           method);
                return asyncRpc;
            }

            private <ReqT, RespT> RetryingUnaryRpcCallListener<ReqT, RespT> createUnaryListener(ReqT request,
                                                                                                AsyncCallClientInternal<ReqT, RespT> rpc) {
                return new RetryingUnaryRpcCallListener<>(retryOptions, request, rpc, callOptions, new Metadata());
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
