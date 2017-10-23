/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.annotation.GrpcMethodType;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.grpc.util.GrpcUtil;
import com.quancheng.saluki.core.utils.ReflectUtils;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

/**
 * @author shimingliu 2016年12月14日 下午5:51:01
 * @version GrpcRequest.java, v 0.0.1 2016年12月14日 下午5:51:01 shimingliu
 */
public interface GrpcRequest {

  public Class<?> getResponseType();

  public MethodDescriptor<Message, Message> getMethodDescriptor();

  public Channel getChannel();

  public void returnChannel(Channel channel);

  public String getServiceName();

  public String getMethodName();

  public Object getRequestParam();

  public Object getResponseOberver();

  public GrpcURL getRefUrl();

  public int getCallType();

  public int getCallTimeout();

  public io.grpc.MethodDescriptor.MethodType getMethodType();


  public static class Default implements GrpcRequest, Serializable {

    private static final long serialVersionUID = 1L;

    private final GrpcURL refUrl;

    private final GrpcProtocolClient.ChannelCall chanelPool;

    private final String methodName;

    private final Object[] args;

    private final int callType;

    private final int callTimeout;

    private final GrpcMethodType grpcMethodType;

    public Default(GrpcURL refUrl, GrpcProtocolClient.ChannelCall chanelPool, String methodName,
        Object[] args, int callType, int callTimeout) {
      super();
      this.refUrl = refUrl.addParameter(Constants.METHOD_KEY, methodName);;
      this.chanelPool = chanelPool;
      this.methodName = methodName;
      if (args.length > 2) {
        throw new IllegalArgumentException(
            "grpc not support multiple args,args is " + args + " length is " + args.length);
      } else {
        this.args = args;
      }
      this.callType = callType;
      this.callTimeout = callTimeout;
      try {
        Class<?> service = ReflectUtils.forName(this.getServiceName());
        Method method = ReflectUtils.findMethodByMethodName(service, this.getMethodName());
        grpcMethodType = method.getAnnotation(GrpcMethodType.class);
      } catch (Exception e) {
        RpcFrameworkException framworkException = new RpcFrameworkException(e);
        throw framworkException;
      }
    }

    @Override
    public Object getRequestParam() {
      return args[0];
    }

    @Override
    public MethodDescriptor<Message, Message> getMethodDescriptor() {
      return GrpcUtil.createMethodDescriptor(this.getServiceName(), methodName, grpcMethodType);
    }

    @Override
    public Class<?> getResponseType() {
      return grpcMethodType.responseType();
    }

    @Override
    public Channel getChannel() {
      return chanelPool.borrowChannel(refUrl);
    }

    @Override
    public void returnChannel(Channel channel) {
      chanelPool.returnChannel(refUrl, channel);
    }

    @Override
    public String getServiceName() {
      return refUrl.getServiceInterface();
    }

    @Override
    public GrpcURL getRefUrl() {
      return this.refUrl;
    }


    @Override
    public String getMethodName() {
      return this.methodName;
    }

    @Override
    public int getCallType() {
      return this.callType;
    }

    @Override
    public int getCallTimeout() {
      return this.callTimeout;
    }

    @Override
    public io.grpc.MethodDescriptor.MethodType getMethodType() {
      return this.grpcMethodType.methodType();
    }


    @Override
    public Object getResponseOberver() {
      return args[1];
    }

  }

}
