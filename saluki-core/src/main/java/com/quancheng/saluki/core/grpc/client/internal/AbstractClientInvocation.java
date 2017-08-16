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

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.stub.ClientCalls;
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
    monitor = ClientServerMonitor.newClientServerMonitor(monitorinterval);
    requstValidator = RequestValidator.newRequestValidator();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (ReflectUtils.isToStringMethod(method)) {
      return AbstractClientInvocation.this.toString();
    } else {
      GrpcRequest request = this.buildGrpcRequest(method, args);
      requstValidator.doValidate(request);
      MethodType methodType = request.getMethodType();
      Channel channel = request.getChannel();
      MethodDescriptor<Message, Message> methodDesc = request.getMethodDescriptor();
      ClientCall<Message, Message> clientCall = channel.newCall(methodDesc, CallOptions.DEFAULT);
      Object param = request.getRequestParam();
      switch (methodType) {
        case UNARY:
          return doUnaryCall(request, channel);
        case CLIENT_STREAMING:
          if (param instanceof StreamObserver) {
            StreamObserver<Message> requestObserver =
                ClientCalls.asyncClientStreamingCall(clientCall,
                    Proto2PoJoStreamObserver.newObserverWrap((StreamObserver<Object>) param));
            return PoJo2ProtoStreamObserver.newObserverWrap(requestObserver);
          }
        case SERVER_STREAMING:
          Object responseObserver = args[1];
          if (responseObserver instanceof StreamObserver) {
            try {
              Message messageParam = SerializerUtil.pojo2Protobuf(param);
              ClientCalls.asyncServerStreamingCall(clientCall, messageParam,
                  Proto2PoJoStreamObserver.newObserverWrap((StreamObserver<Object>) param));
            } catch (ProtobufException e) {
              RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
              throw rpcFramwork;
            }
          }
        case BIDI_STREAMING:
          if (param instanceof StreamObserver) {
            StreamObserver<Message> requestObserver = ClientCalls.asyncBidiStreamingCall(clientCall,
                Proto2PoJoStreamObserver.newObserverWrap((StreamObserver<Object>) param));
            return PoJo2ProtoStreamObserver.newObserverWrap(requestObserver);
          }
        default:
          RpcServiceException rpcFramwork =
              new RpcServiceException(RpcErrorMsgConstant.SERVICE_UNFOUND);
          throw rpcFramwork;
      }
    }
  }


  private Object doUnaryCall(GrpcRequest request, Channel channel) {
    String serviceName = request.getServiceName();
    String methodName = request.getMethodName();
    GrpcURL refUrl = request.getRefUrl();
    Integer retryOption = this.buildRetryOption(methodName, refUrl);
    GrpcUnaryClientCall clientCall = GrpcUnaryClientCall.create(channel, retryOption, refUrl);
    try {
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
    } finally {
      Object remote = clientCall.getAffinity().get(GrpcUnaryClientCall.GRPC_CURRENT_ADDR_KEY);
      log.info(String.format("Service: %s  Method: %s  RemoteAddress: %s", serviceName, methodName,
          String.valueOf(remote)));
      request.returnChannel(channel);
    }
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
