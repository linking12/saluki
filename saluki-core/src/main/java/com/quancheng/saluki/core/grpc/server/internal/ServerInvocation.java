/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.server.internal;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.annotation.GrpcMethodType;
import com.quancheng.saluki.core.grpc.service.MonitorService;
import com.quancheng.saluki.core.grpc.stream.PoJo2ProtoStreamObserver;
import com.quancheng.saluki.core.grpc.stream.Proto2PoJoStreamObserver;
import com.quancheng.saluki.core.grpc.util.SerializerUtil;
import com.quancheng.saluki.core.utils.ReflectUtils;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCalls.BidiStreamingMethod;
import io.grpc.stub.ServerCalls.ClientStreamingMethod;
import io.grpc.stub.ServerCalls.ServerStreamingMethod;
import io.grpc.stub.StreamObserver;
import io.netty.util.internal.ThrowableUtil;

/**
 * @author shimingliu 2016年12月14日 下午10:14:18
 * @version ServerInvocation.java, v 0.0.1 2016年12月14日 下午10:14:18 shimingliu
 */
public class ServerInvocation implements io.grpc.stub.ServerCalls.UnaryMethod<Message, Message>,
    ServerStreamingMethod<Message, Message>, ClientStreamingMethod<Message, Message>,
    BidiStreamingMethod<Message, Message> {

  private static final Logger log = LoggerFactory.getLogger(ServerInvocation.class);

  private final MonitorService salukiMonitor;

  private final Object serviceToInvoke;

  private final Method method;

  private final GrpcURL providerUrl;

  private final GrpcMethodType grpcMethodType;

  private final ConcurrentMap<String, AtomicInteger> concurrents;

  public ServerInvocation(Object serviceToInvoke, Method method, GrpcMethodType grpcMethodType,
      GrpcURL providerUrl, ConcurrentMap<String, AtomicInteger> concurrents,
      MonitorService salukiMonitor) {
    this.serviceToInvoke = serviceToInvoke;
    this.method = method;
    this.grpcMethodType = grpcMethodType;
    this.salukiMonitor = salukiMonitor;
    this.providerUrl = providerUrl;
    this.concurrents = concurrents;
  }

  @SuppressWarnings("unchecked")
  @Override
  public StreamObserver<Message> invoke(StreamObserver<Message> responseObserver) {
    try {
      PoJo2ProtoStreamObserver servserResponseObserver =
          PoJo2ProtoStreamObserver.newObserverWrap(responseObserver);
      Object result = method.invoke(serviceToInvoke, servserResponseObserver);
      return Proto2PoJoStreamObserver.newObserverWrap((StreamObserver<Object>) result,
          grpcMethodType.responseType());
    } catch (Throwable e) {
      String stackTrace = ThrowableUtil.stackTraceToString(e);
      log.error(e.getMessage(), e);
      StatusRuntimeException statusException =
          Status.UNAVAILABLE.withDescription(stackTrace).asRuntimeException();
      responseObserver.onError(statusException);
    }
    return null;

  }

  @Override
  public void invoke(Message request, StreamObserver<Message> responseObserver) {
    Message reqProtoBufer = request;
    Message respProtoBufer = null;
    long start = System.currentTimeMillis();
    try {
      getConcurrent().getAndIncrement();
      Class<?> requestType = ReflectUtils.getTypedOfReq(method);
      Object reqPojo = SerializerUtil.protobuf2Pojo(reqProtoBufer, requestType);
      Object[] requestParams = null;
      switch (grpcMethodType.methodType()) {
        case UNARY:
          requestParams = new Object[] {reqPojo};
          Object respPojo = method.invoke(serviceToInvoke, requestParams);
          respProtoBufer = SerializerUtil.pojo2Protobuf(respPojo);
          collect(reqProtoBufer, respProtoBufer, start, false);
          responseObserver.onNext(respProtoBufer);
          responseObserver.onCompleted();
        case SERVER_STREAMING:
          requestParams =
              new Object[] {reqPojo, PoJo2ProtoStreamObserver.newObserverWrap(responseObserver)};
          method.invoke(serviceToInvoke, requestParams);
          break;
        default:
          break;
      }
    } catch (Throwable e) {
      String stackTrace = ThrowableUtil.stackTraceToString(e);
      log.error(e.getMessage(), e);
      collect(reqProtoBufer, respProtoBufer, start, true);
      StatusRuntimeException statusException =
          Status.UNAVAILABLE.withDescription(stackTrace).asRuntimeException();
      responseObserver.onError(statusException);
    } finally {
      log.info(String.format("Service: %s  Method: %s  RemoteAddress: %s",
          providerUrl.getServiceInterface(), method.getName(),
          RpcContext.getContext().getAttachment(Constants.REMOTE_ADDRESS)));
      getConcurrent().decrementAndGet();
    }
  }

  // 信息采集
  private void collect(Message request, Message response, long start, boolean error) {
    try {
      if (request == null || response == null) {
        return;
      }
      long elapsed = System.currentTimeMillis() - start; // 计算调用耗时
      int concurrent = getConcurrent().get(); // 当前并发数
      String service = providerUrl.getServiceInterface(); // 获取服务名称
      String method = this.method.getName(); // 获取方法名
      String consumer = RpcContext.getContext().getAttachment(Constants.REMOTE_ADDRESS);// 远程服务器地址
      String host = providerUrl.getHost();
      int rpcPort = providerUrl.getPort();
      int registryRpcPort = providerUrl.getParameter(Constants.REGISTRY_RPC_PORT_KEY, rpcPort);
      salukiMonitor.collect(new GrpcURL(Constants.MONITOR_PROTOCOL, host, //
          registryRpcPort, //
          service + "/" + method, //
          MonitorService.TIMESTAMP, String.valueOf(start), //
          MonitorService.APPLICATION, providerUrl.getParameter(Constants.APPLICATION_NAME), //
          MonitorService.INTERFACE, service, //
          MonitorService.METHOD, method, //
          MonitorService.CONSUMER, consumer, //
          error ? MonitorService.FAILURE : MonitorService.SUCCESS, "1", //
          MonitorService.ELAPSED, String.valueOf(elapsed), //
          MonitorService.CONCURRENT, String.valueOf(concurrent), //
          MonitorService.INPUT, String.valueOf(request.getSerializedSize()), //
          MonitorService.OUTPUT, String.valueOf(response.getSerializedSize())));
    } catch (Throwable t) {
      log.warn("Failed to monitor count service " + this.serviceToInvoke.getClass() + ", cause: "
          + t.getMessage());
    }

  }

  public String getRpcName() {
    return this.providerUrl.getServiceInterface() + ":" + method.getName();
  }

  public String getLocalAddressString() {
    return this.providerUrl.getAddress();
  }

  private AtomicInteger getConcurrent() {
    String key = serviceToInvoke.getClass().getName() + "." + method.getName();
    AtomicInteger concurrent = concurrents.get(key);
    if (concurrent == null) {
      concurrents.putIfAbsent(key, new AtomicInteger());
      concurrent = concurrents.get(key);
    }
    return concurrent;
  }
}
