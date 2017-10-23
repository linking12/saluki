/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.internal.unary;



import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.client.internal.GrpcCallOptions;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

/**
 * @author shimingliu 2016年12月14日 下午9:54:44
 * @version GrpcAsyncCall.java, v 0.0.1 2016年12月14日 下午9:54:44 shimingliu
 */
public interface GrpcUnaryClientCall {

  public ListenableFuture<Message> unaryFuture(Message request,
      MethodDescriptor<Message, Message> method);

  public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method);

  public static GrpcUnaryClientCall create(final Channel channel, final Integer retryOptions,
      final GrpcURL refUrl) {
    final CallOptions callOptions = GrpcCallOptions.createCallOptions(refUrl);
    return new GrpcUnaryClientCall() {

      private FailOverUnaryFuture<Message, Message> newFailOverUnaryFuture(
          final MethodDescriptor<Message, Message> method) {
        if (FAILOVER_UNARAY_FUTRURES.containsKey(method)) {
          return FAILOVER_UNARAY_FUTRURES.get(method);
        } else {
          return new FailOverUnaryFuture<Message, Message>(method);
        }
      }


      @Override
      public ListenableFuture<Message> unaryFuture(Message request,
          MethodDescriptor<Message, Message> method) {
        FailOverUnaryFuture<Message, Message> retryCallListener = newFailOverUnaryFuture(method);
        retryCallListener.setRequest(request);
        retryCallListener.setMaxRetries(retryOptions);
        retryCallListener.setChannel(channel);
        retryCallListener.setCallOptions(callOptions);
        retryCallListener.run();
        return retryCallListener.getFuture();
      }

      @Override
      public Message blockingUnaryResult(Message request,
          MethodDescriptor<Message, Message> method) {
        FailOverUnaryFuture<Message, Message> retryCallListener = newFailOverUnaryFuture(method);
        retryCallListener.setRequest(request);
        retryCallListener.setMaxRetries(retryOptions);
        retryCallListener.setChannel(channel);
        retryCallListener.setCallOptions(callOptions);
        try {
          retryCallListener.run();
          return retryCallListener.getFuture().get();
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

  static final Map<MethodDescriptor<Message, Message>, FailOverUnaryFuture<Message, Message>> FAILOVER_UNARAY_FUTRURES =
      Collections.synchronizedMap(
          new WeakHashMap<MethodDescriptor<Message, Message>, FailOverUnaryFuture<Message, Message>>());

}
