/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
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
public class FailOverListener<Request, Response> extends ClientCall.Listener<Response>
    implements Runnable {

  private final static Logger logger = LoggerFactory.getLogger(FailOverListener.class);

  private final ExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor();

  private final CompletionFuture<Response> completionFuture = new CompletionFuture<Response>();

  private final AtomicInteger retries = new AtomicInteger(0);

  private final Integer retriesOptions;

  private final Channel channel;

  private final MethodDescriptor<Request, Response> method;

  private final Request request;

  private final CallOptions callOptions;

  private final NameResolverNotify nameResolverNotify;

  private volatile ClientCall<Request, Response> clientCall;

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

  public FailOverListener(final Integer retriesOptions, final Request request,
      final Channel channel, final MethodDescriptor<Request, Response> method,
      final CallOptions callOptions) {
    this.retriesOptions = retriesOptions;
    this.request = request;
    this.channel = channel;
    this.method = method;
    this.callOptions = callOptions;
    this.nameResolverNotify = new NameResolverNotify();
  }

  @Override
  public void onMessage(Response message) {
    completionFuture.set(message);
  }


  @Override
  public void onClose(Status status, Metadata trailers) {
    Map<String, Object> affinity = callOptions.getOption(GrpcClientCall.CALLOPTIONS_CUSTOME_KEY);
    SocketAddress currentServer = clientCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
    affinity.put(GrpcClientCall.GRPC_CURRENT_ADDR_KEY, currentServer);
    nameResolverNotify.refreshAffinity(affinity);
    Status.Code code = status.getCode();
    if (code == Status.Code.OK) {
      if (retries.get() > 0) {
        nameResolverNotify.resetChannel();
      }
      retries.set(0);
      return;
    } else {
      if (retries.get() >= retriesOptions || retriesOptions == 0) {
        completionFuture.setException(status.asRuntimeException());
        nameResolverNotify.resetChannel();
        return;
      } else {
        logger.error(String.format("Retrying failed call. Failure #%d，Failure Server: %s",
            retries.get(), String.valueOf(currentServer)));
        nameResolverNotify.refreshChannel();
        retryExecutor.execute(this);
        retries.getAndIncrement();
      }
    }
  }

  @Override
  public void run() {
    clientCall = channel.newCall(method, callOptions);
    RpcContext.getContext().removeAttachment("routerRule");
    clientCall.start(this, new Metadata());
    clientCall.sendMessage(request);
    clientCall.halfClose();
    clientCall.request(1);
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
