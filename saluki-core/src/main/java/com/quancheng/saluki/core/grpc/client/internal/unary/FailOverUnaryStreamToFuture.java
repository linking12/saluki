/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.internal.unary;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.quancheng.saluki.core.grpc.client.internal.GrpcCallOptions;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.internal.GrpcUtil;

/**
 * @author liushiming 2017年5月2日 下午5:42:42
 * @version FailOverListener.java, v 0.0.1 2017年5月2日 下午5:42:42 liushiming
 */
public class FailOverUnaryStreamToFuture<Request, Response> extends ClientCall.Listener<Response>
    implements Runnable {

  private final static Logger logger = LoggerFactory.getLogger(FailOverUnaryStreamToFuture.class);

  private final ScheduledExecutorService scheduleRetryService = GrpcUtil.TIMER_SERVICE.create();

  private final AtomicInteger currentRetries = new AtomicInteger(0);

  private final Integer maxRetries;

  private final Channel channel;

  private final MethodDescriptor<Request, Response> method;

  private final CallOptions callOptions;

  private final boolean enabledRetry;

  private CompletionFuture<Response> completionFuture;

  private ClientCall<Request, Response> clientCall;

  private Request request;

  private Response response;

  public FailOverUnaryStreamToFuture(final Integer retriesOptions, final Channel channel,
      final MethodDescriptor<Request, Response> method, final CallOptions callOptions) {
    this.maxRetries = retriesOptions;
    this.channel = channel;
    this.method = method;
    this.callOptions = callOptions;
    this.enabledRetry = maxRetries > 0 ? true : false;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  @Override
  public void onMessage(Response message) {
    if (this.response != null && !enabledRetry) {
      throw Status.INTERNAL.withDescription("More than one value received for unary call")
          .asRuntimeException();
    }
    this.response = message;
  }


  @Override
  public void onClose(Status status, Metadata trailers) {
    try {
      SocketAddress remoteServer = clientCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
      callOptions.getOption(GrpcCallOptions.CALLOPTIONS_CUSTOME_KEY)
          .put(GrpcCallOptions.GRPC_CURRENT_ADDR_KEY, remoteServer);
    } finally {
      if (status.isOk()) {
        statusOk(trailers);
      } else {
        statusError(status, trailers);
      }
    }
  }

  private void statusOk(Metadata trailers) {
    try {
      if (enabledRetry) {
        final NameResolverNotify nameResolverNotify = this.createNameResolverNotify();
        nameResolverNotify.resetChannel();
      }
    } finally {
      if (response == null) {
        completionFuture.setException(Status.INTERNAL
            .withDescription("No value received for unary call").asRuntimeException(trailers));
      }
      completionFuture.set(response);
    }
  }


  private void statusError(Status status, Metadata trailers) {
    if (enabledRetry) {
      final NameResolverNotify nameResolverNotify = this.createNameResolverNotify();
      boolean retryHaveDone = this.retryHaveDone();
      if (retryHaveDone) {
        completionFuture.setException(status.asRuntimeException(trailers));
      } else {
        nameResolverNotify.refreshChannel();
        scheduleRetryService.execute(this);
        SocketAddress remoteAddress =
            (SocketAddress) callOptions.getOption(GrpcCallOptions.CALLOPTIONS_CUSTOME_KEY)
                .get(GrpcCallOptions.GRPC_CURRENT_ADDR_KEY);
        logger.error(String.format("Retrying failed call. Failure #%d，Failure Server: %s",
            currentRetries.get(), String.valueOf(remoteAddress)));
        currentRetries.getAndIncrement();
      }
    } else {
      completionFuture.setException(status.asRuntimeException(trailers));
    }

  }

  private NameResolverNotify createNameResolverNotify() {
    Map<String, Object> affinity = callOptions.getOption(GrpcCallOptions.CALLOPTIONS_CUSTOME_KEY);
    NameResolverNotify nameResolverNotify = NameResolverNotify.newNameResolverNotify();
    nameResolverNotify.refreshAffinity(affinity);
    return nameResolverNotify;
  }

  private boolean retryHaveDone() {
    return currentRetries.get() >= maxRetries;
  }

  @Override
  public void run() {
    this.clientCall = channel.newCall(method, callOptions);
    this.completionFuture = new CompletionFuture<Response>(this.clientCall);
    this.clientCall.start(this, new Metadata());
    this.clientCall.sendMessage(request);
    this.clientCall.halfClose();
    this.clientCall.request(1);
  }


  public ListenableFuture<Response> getFuture() {
    return completionFuture;
  }

  public void cancel() {
    if (clientCall != null) {
      clientCall.cancel("User requested cancelation.", null);
    }
  }

}
