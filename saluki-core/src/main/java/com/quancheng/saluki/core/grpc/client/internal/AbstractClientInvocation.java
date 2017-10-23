/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.client.GrpcRequest;
import com.quancheng.saluki.core.grpc.client.internal.stream.GrpcStreamClientCall;
import com.quancheng.saluki.core.grpc.client.internal.unary.GrpcBlockingUnaryCommand;
import com.quancheng.saluki.core.grpc.client.internal.unary.GrpcFutureUnaryCommand;
import com.quancheng.saluki.core.grpc.client.internal.unary.GrpcHystrixCommand;
import com.quancheng.saluki.core.grpc.client.internal.unary.GrpcUnaryClientCall;
import com.quancheng.saluki.core.grpc.client.internal.validate.RequestValidator;
import com.quancheng.saluki.core.grpc.exception.RpcErrorMsgConstant;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.grpc.exception.RpcServiceException;
import com.quancheng.saluki.core.grpc.service.ClientServerMonitor;
import com.quancheng.saluki.core.grpc.stream.PoJo2ProtoStreamObserver;
import com.quancheng.saluki.core.grpc.stream.Proto2PoJoStreamObserver;
import com.quancheng.saluki.core.grpc.util.SerializerUtil;
import com.quancheng.saluki.core.utils.ReflectUtils;
import com.quancheng.saluki.serializer.exception.ProtobufException;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.stub.StreamObserver;

/**
 * @author shimingliu 2016年12月14日 下午9:38:34
 * @version AbstractClientInvocation.java, v 0.0.1 2016年12月14日 下午9:38:34 shimingliu
 */
public abstract class AbstractClientInvocation implements InvocationHandler {

  private static final Logger log = LoggerFactory.getLogger(AbstractClientInvocation.class);

  private final ClientServerMonitor monitor;

  private final RequestValidator requstValidator;

  protected abstract GrpcRequest buildGrpcRequest(Method method, Object[] args);


  public AbstractClientInvocation(GrpcURL refUrl) {
    Long monitorinterval = refUrl.getParameter("monitorinterval", 60L);
    this.monitor = ClientServerMonitor.newClientServerMonitor(monitorinterval);
    this.requstValidator = RequestValidator.newRequestValidator();
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (ReflectUtils.isToStringMethod(method)) {
      return AbstractClientInvocation.this.toString();
    } else {
      GrpcRequest request = this.buildGrpcRequest(method, args);
      requstValidator.doValidate(request);
      MethodType methodType = request.getMethodType();
      Channel channel = request.getChannel();
      try {
        switch (methodType) {
          case UNARY:
            return unaryCall(request, channel);
          case CLIENT_STREAMING:
            return streamCall(request, channel);
          case SERVER_STREAMING:
            return streamCall(request, channel);
          case BIDI_STREAMING:
            return streamCall(request, channel);
          default:
            RpcServiceException rpcFramwork =
                new RpcServiceException(RpcErrorMsgConstant.SERVICE_UNFOUND);
            throw rpcFramwork;
        }
      } finally {
        Object remote = GrpcCallOptions.getAffinity(request.getRefUrl())
            .get(GrpcCallOptions.GRPC_CURRENT_ADDR_KEY);
        log.debug(String.format("Service: %s  Method: %s  RemoteAddress: %s",
            request.getServiceName(), request.getMethodName(), String.valueOf(remote)));
        request.returnChannel();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Object streamCall(GrpcRequest request, Channel channel) {
    GrpcURL refUrl = request.getRefUrl();
    GrpcStreamClientCall clientCall = GrpcStreamClientCall.create(channel, refUrl);
    MethodType methodType = request.getMethodType();
    Class<?> returnType = request.getResponseType();
    MethodDescriptor<Message, Message> methodDesc = request.getMethodDescriptor();
    Object requestParam = request.getRequestParam();
    StreamObserver<Message> requestObserver;
    switch (methodType) {
      case CLIENT_STREAMING:
        requestObserver = clientCall.asyncClientStream(methodDesc, Proto2PoJoStreamObserver
            .newObserverWrap((StreamObserver<Object>) requestParam, returnType));
        return PoJo2ProtoStreamObserver.newObserverWrap(requestObserver);
      case SERVER_STREAMING:
        Object responseObserver = request.getResponseOberver();
        try {
          Message messageParam = SerializerUtil.pojo2Protobuf(requestParam);
          clientCall.asyncServerStream(methodDesc, Proto2PoJoStreamObserver.newObserverWrap(
              (StreamObserver<Object>) responseObserver, returnType), messageParam);
        } catch (ProtobufException e) {
          RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
          throw rpcFramwork;
        }
        return null;
      case BIDI_STREAMING:
        requestObserver = clientCall.asyncBidiStream(methodDesc, Proto2PoJoStreamObserver
            .newObserverWrap((StreamObserver<Object>) requestParam, returnType));
        return PoJo2ProtoStreamObserver.newObserverWrap(requestObserver);
      default:
        RpcServiceException rpcFramwork =
            new RpcServiceException(RpcErrorMsgConstant.SERVICE_UNFOUND);
        throw rpcFramwork;
    }

  }



  private Object unaryCall(GrpcRequest request, Channel channel) {
    String serviceName = request.getServiceName();
    String methodName = request.getMethodName();
    GrpcURL refUrl = request.getRefUrl();
    Integer retryOption = this.buildRetryOption(methodName, refUrl);
    GrpcUnaryClientCall clientCall = GrpcUnaryClientCall.create(channel, retryOption, refUrl);
    GrpcHystrixCommand hystrixCommand = null;
    Boolean isEnableFallback = this.buildFallbackOption(methodName, refUrl);
    switch (request.getCallType()) {
      case Constants.RPCTYPE_ASYNC:
        hystrixCommand = new GrpcFutureUnaryCommand(serviceName, methodName, isEnableFallback);
        break;
      case Constants.RPCTYPE_BLOCKING:
        hystrixCommand = new GrpcBlockingUnaryCommand(serviceName, methodName, isEnableFallback);
        break;
      default:
        hystrixCommand = new GrpcFutureUnaryCommand(serviceName, methodName, isEnableFallback);
        break;
    }
    hystrixCommand.setClientCall(clientCall);
    hystrixCommand.setRequest(request);
    hystrixCommand.setClientServerMonitor(monitor);
    return hystrixCommand.execute();

  }



  private Boolean buildFallbackOption(String methodName, GrpcURL refUrl) {
    Boolean isEnableFallback = refUrl.getParameter(Constants.GRPC_FALLBACK_KEY, Boolean.FALSE);
    String[] methodNames =
        StringUtils.split(refUrl.getParameter(Constants.FALLBACK_METHODS_KEY), ",");
    if (methodNames != null && methodNames.length > 0) {
      return isEnableFallback && Arrays.asList(methodNames).contains(methodName);
    } else {
      return isEnableFallback;
    }
  }

  private Integer buildRetryOption(String methodName, GrpcURL refUrl) {
    Integer retries = refUrl.getParameter((Constants.METHOD_RETRY_KEY), 0);
    String[] methodNames = StringUtils.split(refUrl.getParameter(Constants.RETRY_METHODS_KEY), ",");
    if (methodNames != null && methodNames.length > 0) {
      if (Arrays.asList(methodNames).contains(methodName)) {
        return retries;
      } else {
        return Integer.valueOf(0);
      }
    } else {
      return retries;
    }
  }

}
