/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.async;

import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;

import io.grpc.Attributes;
import io.grpc.Attributes.Key;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.NameResolver;
import io.grpc.Status;

/**
 * @author shimingliu 2016年12月14日 下午9:54:44
 * @version GrpcAsyncCall.java, v 0.0.1 2016年12月14日 下午9:54:44 shimingliu
 */
public interface GrpcClientCall {

    public static final Attributes.Key<GrpcURL>               GRPC_REF_URL              = Attributes.Key.of("grpc-refurl");

    public static final Attributes.Key<SocketAddress>         CURRENT_ADDR_KEY          = Attributes.Key.of("current-address");

    public static final Attributes.Key<List<SocketAddress>>   REGISTRY_REMOTE_ADDR_KEYS = Attributes.Key.of("registry-remote-addresss");

    public static final Attributes.Key<NameResolver.Listener> NAMERESOVER_LISTENER      = Attributes.Key.of("nameResolver-Listener");

    public ListenableFuture<Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method);

    public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method);

    public Attributes getAffinity();

    public static GrpcClientCall create(final Channel channel, final Integer retryOptions, final GrpcURL refUrl) {
        Attributes affinity = Attributes.newBuilder().set(GrpcClientCall.GRPC_REF_URL, refUrl).build();
        CallOptions callOptions = CallOptions.DEFAULT.withAffinity(affinity);
        return new GrpcClientCall() {

            @Override
            public Attributes getAffinity() {
                return affinity;
            }

            @Override
            public ListenableFuture<Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method) {
                RetryCallListener<Message, Message> retryCallListener = new RetryCallListener<Message, Message>(retryOptions,
                                                                                                                request,
                                                                                                                channel,
                                                                                                                method,
                                                                                                                callOptions);
                retryCallListener.run();
                return retryCallListener.getCompletionFuture();
            }

            @Override
            public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method) {
                RetryCallListener<Message, Message> retryCallListener = new RetryCallListener<Message, Message>(retryOptions,
                                                                                                                request,
                                                                                                                channel,
                                                                                                                method,
                                                                                                                callOptions);
                try {
                    retryCallListener.run();
                    return retryCallListener.getCompletionFuture().get();
                } catch (InterruptedException e) {
                    retryCallListener.cancel();
                    throw Status.CANCELLED.withCause(e).asRuntimeException();
                } catch (ExecutionException e) {
                    retryCallListener.cancel();
                    throw Status.fromThrowable(e).asRuntimeException();
                }
            }

        };
    }

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

}
