/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.failover;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

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
 * @version $Id: RetryCallListener.java, v 0.0.1 2017年5月2日 下午5:42:42 liushiming
 */
public class FailOverListener<Request, Response> extends ClientCall.Listener<Response>
    implements Runnable {

  private final static Logger logger = LoggerFactory.getLogger(FailOverListener.class);

  private final ScheduledExecutorService scheduleRetryService = GrpcUtil.TIMER_SERVICE.create();

  private final AtomicInteger currentRetries = new AtomicInteger(0);

  private final CompletionFuture<Response> completionFuture = new CompletionFuture<Response>();

  private final Integer maxRetries;

  private final Channel channel;

  private final MethodDescriptor<Request, Response> method;

  private final Request request;

  private final CallOptions callOptions;

  private ClientCall<Request, Response> clientCall;

  private Response value;

  private final boolean enabledRetry;

  public FailOverListener(final Integer retriesOptions, final Request request,
      final Channel channel, final MethodDescriptor<Request, Response> method,
      final CallOptions callOptions) {
    this.maxRetries = retriesOptions;
    this.request = request;
    this.channel = channel;
    this.method = method;
    this.callOptions = callOptions;
    this.enabledRetry = maxRetries > 0 ? true : false;
  }

  @Override
  public void onMessage(Response message) {
    if (this.value != null && !enabledRetry) {
      throw Status.INTERNAL.withDescription("More than one value received for unary call")
          .asRuntimeException();
    }
    this.value = message;
  }


  @Override
  public void onClose(Status status, Metadata trailers) {
    try {
      SocketAddress remoteServer = clientCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
      callOptions.getOption(GrpcClientCall.CALLOPTIONS_CUSTOME_KEY)
          .put(GrpcClientCall.GRPC_CURRENT_ADDR_KEY, remoteServer);
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
      if (value == null) {
        completionFuture.setException(Status.INTERNAL
            .withDescription("No value received for unary call").asRuntimeException(trailers));
      }
      completionFuture.set(value);
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
            (SocketAddress) callOptions.getOption(GrpcClientCall.CALLOPTIONS_CUSTOME_KEY)
                .get(GrpcClientCall.GRPC_CURRENT_ADDR_KEY);
        logger.error(String.format("Retrying failed call. Failure #%d，Failure Server: %s",
            currentRetries.get(), String.valueOf(remoteAddress)));
        currentRetries.getAndIncrement();
      }
    } else {
      completionFuture.setException(status.asRuntimeException(trailers));
    }

  }

  private NameResolverNotify createNameResolverNotify() {
    Map<String, Object> affinity = callOptions.getOption(GrpcClientCall.CALLOPTIONS_CUSTOME_KEY);
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
    this.clientCall.start(this, new Metadata());
    this.clientCall.sendMessage(request);
    this.clientCall.halfClose();
    this.clientCall.request(1);
  }


  public ListenableFuture<Response> getCompletionFuture() {
    return completionFuture;
  }

  public void cancel() {
    if (clientCall != null) {
      clientCall.cancel("User requested cancelation.", null);
    }
  }

}
