/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.grpc.client.GrpcRequest;
import com.quancheng.saluki.core.grpc.client.failover.GrpcClientCall;
import com.quancheng.saluki.core.grpc.client.hystrix.GrpcBlockingUnaryCommand;
import com.quancheng.saluki.core.grpc.client.hystrix.GrpcFutureUnaryCommand;
import com.quancheng.saluki.core.grpc.client.hystrix.GrpcHystrixCommand;
import com.quancheng.saluki.core.grpc.service.ClientServerMonitor;
import com.quancheng.saluki.core.utils.ReflectUtils;

import io.grpc.Channel;

/**
 * @author shimingliu 2016年12月14日 下午9:38:34
 * @version AbstractClientInvocation.java, v 0.0.1 2016年12月14日 下午9:38:34 shimingliu
 */
public abstract class AbstractClientInvocation implements InvocationHandler {

  private static final Logger log = LoggerFactory.getLogger(AbstractClientInvocation.class);

  private final Map<String, Integer> methodRetries;

  private final ConcurrentMap<String, AtomicInteger> concurrents = Maps.newConcurrentMap();

  private volatile ClientServerMonitor clientServerMonitor;

  public AbstractClientInvocation(Map<String, Integer> methodRetries) {
    this.methodRetries = methodRetries;
  }

  protected abstract GrpcRequest buildGrpcRequest(Method method, Object[] args);

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (ReflectUtils.isToStringMethod(method)) {
      return AbstractClientInvocation.this.toString();
    }
    GrpcRequest request = this.buildGrpcRequest(method, args);
    if (clientServerMonitor == null) {
      clientServerMonitor = new ClientServerMonitor(request.getRefUrl());
    }
    String serviceName = request.getServiceName();
    String methodName = request.getMethodRequest().getMethodName();
    Channel channel = request.getChannel();
    Integer retryOption = this.buildRetryOption(methodName);
    GrpcClientCall clientCall = GrpcClientCall.create(channel, retryOption, request.getRefUrl());
    try {
      this.calculateConcurrent(serviceName, methodName).incrementAndGet();
      AtomicInteger concurrent = this.calculateConcurrent(serviceName, methodName);
      GrpcHystrixCommand hystrixCommand = null;
      switch (request.getMethodRequest().getCallType()) {
        case Constants.RPCTYPE_ASYNC:
          hystrixCommand = new GrpcFutureUnaryCommand(serviceName, methodName);
          break;
        case Constants.RPCTYPE_BLOCKING:
          hystrixCommand = new GrpcBlockingUnaryCommand(serviceName, methodName);
          break;
        default:
          hystrixCommand = new GrpcFutureUnaryCommand(serviceName, methodName);
          break;
      }
      hystrixCommand.setClientCall(clientCall);
      hystrixCommand.setRequest(request);
      hystrixCommand.setConcurrent(concurrent);
      hystrixCommand.setClientServerMonitor(clientServerMonitor);
      return hystrixCommand.execute();
    } finally {
      Object remote = clientCall.getAffinity().get(GrpcClientCall.GRPC_CURRENT_ADDR_KEY);
      log.info(String.format("Service: %s  Method: %s  RemoteAddress: %s", serviceName, methodName,
          String.valueOf(remote)));
      request.returnChannel(channel);
      this.calculateConcurrent(serviceName, methodName).decrementAndGet();
    }
  }

  private Integer buildRetryOption(String methodName) {
    if (methodRetries.size() == 1 && methodRetries.containsKey("*")) {
      Integer retries = methodRetries.get("*");
      if (retries != null) {
        return retries;
      } else {
        return 0;
      }
    } else {
      Integer retries = methodRetries.get(methodName);
      if (retries != null) {
        return retries;
      } else {
        return 0;
      }
    }
  }

  private AtomicInteger calculateConcurrent(String servcieName, String methodName) {
    String key = servcieName + ":" + methodName;
    AtomicInteger concurrent = concurrents.get(key);
    if (concurrent == null) {
      concurrents.putIfAbsent(key, new AtomicInteger());
      concurrent = concurrents.get(key);
    }
    return concurrent;
  }
}
