/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.util.GrpcUtil;
import com.quancheng.saluki.core.grpc.util.SerializerUtil;
import com.quancheng.saluki.serializer.exception.ProtobufException;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

/**
 * @author shimingliu 2016年12月14日 下午5:51:01
 * @version GrpcRequest.java, v 0.0.1 2016年12月14日 下午5:51:01 shimingliu
 */
public interface GrpcRequest {

  public Message getRequestArg() throws ProtobufException;

  public Class<?> getResponseType();

  public MethodDescriptor<Message, Message> getMethodDescriptor();

  public Channel getChannel();

  public void returnChannel(Channel channel);

  public String getServiceName();

  public String getMethodName();

  public GrpcURL getRefUrl();

  public Object getArg();

  public int getCallType();

  public int getCallTimeout();

  public static class Default implements GrpcRequest, Serializable {

    private static final long serialVersionUID = 1L;

    private final GrpcURL refUrl;

    private final GrpcProtocolClient.ChannelCall chanelPool;

    private final String methodName;

    private final Object arg;

    private final int callType;

    private final int callTimeout;

    public Default(GrpcURL refUrl, GrpcProtocolClient.ChannelCall chanelPool, String methodName,
        Object arg, int callType, int callTimeout) {
      super();
      this.refUrl = refUrl;
      this.chanelPool = chanelPool;
      this.methodName = methodName;
      this.arg = arg;
      this.callType = callType;
      this.callTimeout = callTimeout;
    }

    @Override
    public Message getRequestArg() throws ProtobufException {
      return SerializerUtil.pojo2Protobuf(arg);
    }

    @Override
    public MethodDescriptor<Message, Message> getMethodDescriptor() {
      return GrpcUtil.createMethodDescriptor(this.getServiceName(), methodName);
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
      return this.refUrl.addParameter(Constants.METHOD_KEY, methodName)//
          .addParameterAndEncoded(Constants.ARG_KEY, new Gson().toJson(arg));
    }


    @Override
    public String getMethodName() {
      return this.methodName;
    }

    @Override
    public Object getArg() {
      return this.arg;
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
    public Class<?> getResponseType() {
      return GrpcUtil.getResponseType(this.getServiceName(), methodName);
    }

  }

}
