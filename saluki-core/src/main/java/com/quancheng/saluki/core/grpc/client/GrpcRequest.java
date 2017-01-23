/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.util.MethodDescriptorUtil;
import com.quancheng.saluki.core.grpc.util.SerializerUtils;
import com.quancheng.saluki.serializer.exception.ProtobufException;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

/**
 * @author shimingliu 2016年12月14日 下午5:51:01
 * @version GrpcRequest.java, v 0.0.1 2016年12月14日 下午5:51:01 shimingliu
 */
public interface GrpcRequest {

    public Message getRequestArg() throws ProtobufException;

    public MethodDescriptor<Message, Message> getMethodDescriptor();

    public Channel getChannel();

    public void returnChannel(Channel channel);

    public String getServiceName();

    public GrpcURL getRefUrl();

    public MethodRequest getMethodRequest();

    public void setMethodRequest(MethodRequest methodRequest);

    public static class Default implements GrpcRequest, Serializable {

        private static final long                    serialVersionUID = 1L;

        private final GrpcURL                        refUrl;

        private final GrpcProtocolClient.ChannelPool chanelPool;

        private MethodRequest                        methodRequest;

        public Default(GrpcURL refUrl, GrpcProtocolClient.ChannelPool chanelPool){
            super();
            this.refUrl = refUrl;
            this.chanelPool = chanelPool;
        }

        @Override
        public Message getRequestArg() throws ProtobufException {
            Object arg = this.getMethodRequest().getArg();
            return SerializerUtils.Pojo2Protobuf(arg);
        }

        @Override
        public MethodDescriptor<Message, Message> getMethodDescriptor() {
            Message argsReq = MethodDescriptorUtil.buildDefaultInstance(this.getMethodRequest().getRequestType());
            Message argsRep = MethodDescriptorUtil.buildDefaultInstance(this.getMethodRequest().getResponseType());
            return MethodDescriptorUtil.createMethodDescriptor(this.getServiceName(),
                                                               this.getMethodRequest().getMethodName(), argsReq,
                                                               argsRep);
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
        public MethodRequest getMethodRequest() {
            return methodRequest;
        }

        @Override
        public void setMethodRequest(MethodRequest methodRequest) {
            this.methodRequest = methodRequest;
        }

        @Override
        public GrpcURL getRefUrl() {
            Object arg = this.methodRequest.getArg();
            return this.refUrl.addParameter(Constants.METHOD_KEY, this.methodRequest.getMethodName())//
                              .addParameterAndEncoded(Constants.ARG_KEY, new Gson().toJson(arg));
        }

    }

    public static class MethodRequest implements Serializable {

        private static final long serialVersionUID = 5280935790994972153L;

        private final String      methodName;

        private final Class<?>    requestType;

        private final Class<?>    responseType;

        private final Object      arg;

        private final int         callType;

        private final int         callTimeout;

        public MethodRequest(String methodName, Class<?> requestType, Class<?> responseType, Object arg, int callType,
                             int callTimeout){
            super();
            this.methodName = methodName;
            this.requestType = requestType;
            this.responseType = responseType;
            this.arg = arg;
            this.callType = callType;
            this.callTimeout = callTimeout;
        }

        public String getMethodName() {
            return methodName;
        }

        public Class<?> getRequestType() {
            return requestType;
        }

        public Class<?> getResponseType() {
            return responseType;
        }

        public Object getArg() {
            return arg;
        }

        public int getCallType() {
            return callType;
        }

        public int getCallTimeout() {
            return callTimeout;
        }

    }
}
