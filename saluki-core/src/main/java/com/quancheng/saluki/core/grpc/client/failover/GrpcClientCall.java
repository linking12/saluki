/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.failover;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.GrpcURL;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

/**
 * @author shimingliu 2016年12月14日 下午9:54:44
 * @version GrpcAsyncCall.java, v 0.0.1 2016年12月14日 下午9:54:44 shimingliu
 */
public interface GrpcClientCall {

  public static final CallOptions.Key<ConcurrentHashMap<String, Object>> CALLOPTIONS_CUSTOME_KEY =
      CallOptions.Key.of("custom_options", new ConcurrentHashMap<String, Object>());

  public static final String GRPC_REF_URL = "grpc-refurl";

  public static final String GRPC_CURRENT_ADDR_KEY = "current-address";

  public static final String GRPC_NAMERESOVER_ATTRIBUTES = "nameresolver-attributes";

  public ListenableFuture<Message> unaryFuture(Message request,
      MethodDescriptor<Message, Message> method);

  public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method);

  public Map<String, Object> getAffinity();

  public static GrpcClientCall create(final Channel channel, final Integer retryOptions,
      final GrpcURL refUrl) {
    ConcurrentHashMap<String, Object> customOptions = new ConcurrentHashMap<String, Object>();
    customOptions.put(GRPC_REF_URL, refUrl);
    CallOptions callOptions =
        CallOptions.DEFAULT.withOption(CALLOPTIONS_CUSTOME_KEY, customOptions);
    return new GrpcClientCall() {

      @Override
      public Map<String, Object> getAffinity() {
        return callOptions.getOption(CALLOPTIONS_CUSTOME_KEY);
      }

      @Override
      public ListenableFuture<Message> unaryFuture(Message request,
          MethodDescriptor<Message, Message> method) {
        FailOverListener<Message, Message> retryCallListener =
            new FailOverListener<Message, Message>(retryOptions, request, channel, method,
                callOptions);
        retryCallListener.run();
        return retryCallListener.getCompletionFuture();
      }

      @Override
      public Message blockingUnaryResult(Message request,
          MethodDescriptor<Message, Message> method) {
        FailOverListener<Message, Message> retryCallListener =
            new FailOverListener<Message, Message>(retryOptions, request, channel, method,
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

}
