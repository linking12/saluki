/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.internal.unary;

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
    CallOptions callOptions = GrpcCallOptions.createCallOptions(refUrl);
    return new GrpcUnaryClientCall() {

      @Override
      public ListenableFuture<Message> unaryFuture(Message request,
          MethodDescriptor<Message, Message> method) {
        FailOverUnaryStreamToFuture<Message, Message> retryCallListener =
            new FailOverUnaryStreamToFuture<Message, Message>(retryOptions, channel, method,
                callOptions);
        retryCallListener.setRequest(request);
        retryCallListener.run();
        return retryCallListener.getFuture();
      }

      @Override
      public Message blockingUnaryResult(Message request,
          MethodDescriptor<Message, Message> method) {
        FailOverUnaryStreamToFuture<Message, Message> retryCallListener =
            new FailOverUnaryStreamToFuture<Message, Message>(retryOptions, channel, method,
                callOptions);
        retryCallListener.setRequest(request);
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

}
