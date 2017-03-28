/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.client.GrpcClassLoader;
import com.quancheng.saluki.core.grpc.client.GrpcProtocolClient;
import com.quancheng.saluki.core.grpc.client.GrpcRequest;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.ReflectUtils;

/**
 * @author shimingliu 2016年12月14日 下午9:50:27
 * @version GenericProxyClient.java, v 0.0.1 2016年12月14日 下午9:50:27 shimingliu
 */
public class GenericProxyClient<T> implements GrpcProtocolClient<T> {

    private final Map<String, Integer> methodRetries;

    private final GrpcURL              refUrl;

    public GenericProxyClient(Map<String, Integer> methodRetries, GrpcURL refUrl){
        this.methodRetries = methodRetries;
        this.refUrl = refUrl;
    }

    private Class<?> doLoadClass(String className) {
        try {
            @SuppressWarnings("resource")
            GrpcClassLoader classLoader = new GrpcClassLoader();
            classLoader.setSystemClassLoader(Thread.currentThread().getContextClassLoader());
            return classLoader.loadClass(className);
        } catch (Exception e) {
            throw new IllegalArgumentException("grpc  responseType must instanceof com.google.protobuf.GeneratedMessageV3",
                                               e);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public T getGrpcClient(GrpcProtocolClient.ChannelPool channelPool, int callType, int callTimeout) {
        return (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { GenericService.class },
                                          new GenericProxyClientInvocation(channelPool, callType, callTimeout));
    }

    private class GenericProxyClientInvocation extends AbstractClientInvocation {

        private final GrpcProtocolClient.ChannelPool channelPool;
        private final int                            callType;
        private final int                            callTimeout;

        public GenericProxyClientInvocation(GrpcProtocolClient.ChannelPool channelPool, int callType, int callTimeout){
            super(GenericProxyClient.this.methodRetries);
            this.channelPool = channelPool;
            this.callType = callType;
            this.callTimeout = callTimeout;
        }

        @Override
        protected GrpcRequest buildGrpcRequest(Method method, Object[] args) {
            GrpcURL resetRefUrl = GenericProxyClient.this.refUrl;
            resetRefUrl = resetRefUrl.setPath(getServiceName(args));
            resetRefUrl = resetRefUrl.addParameter(Constants.GROUP_KEY, getGroup(args));
            resetRefUrl = resetRefUrl.addParameter(Constants.VERSION_KEY, getVersion(args));
            GrpcRequest request = new GrpcRequest.Default(resetRefUrl, channelPool);
            GrpcRequest.MethodRequest methodRequest = new GrpcRequest.MethodRequest(this.getMethod(args),
                                                                                    this.getReqAndRepType(args).get(0),
                                                                                    this.getReqAndRepType(args).get(1),
                                                                                    this.getArg(args), callType,
                                                                                    callTimeout);
            request.setMethodRequest(methodRequest);
            return request;
        }

        private String getServiceName(Object[] args) {
            return (String) args[0];
        }

        private String getGroup(Object[] args) {
            return (String) args[1];
        }

        private String getVersion(Object[] args) {
            return (String) args[2];
        }

        private String getMethod(Object[] args) {
            return (String) args[3];
        }

        private List<Class<?>> getReqAndRepType(Object[] args) {
            String[] paramType = (String[]) args[4];
            int length = paramType.length;
            if (length != 2) {
                throw new IllegalArgumentException("generic call request type and response type must transmit"
                                                   + " but length is  " + length);
            }
            List<Class<?>> requestAndResponse = Lists.newArrayList();
            String requestType_ = paramType[0];
            String responseType_ = paramType[1];
            Class<?> requestType;
            Class<?> responseType;
            try {
                requestType = ReflectUtils.name2class(requestType_);
            } catch (ClassNotFoundException e) {
                requestType = GenericProxyClient.this.doLoadClass(requestType_);
            }
            try {
                responseType = ReflectUtils.name2class(responseType_);
            } catch (ClassNotFoundException e) {
                responseType = GenericProxyClient.this.doLoadClass(responseType_);
            }
            requestAndResponse.add(requestType);
            requestAndResponse.add(responseType);
            return requestAndResponse;

        }

        private Object getArg(Object[] args) {
            Object[] param = (Object[]) args[5];
            if (param.length != 1) {
                throw new IllegalArgumentException("grpc not support multiple args,args is " + args + " length is "
                                                   + args.length);
            }
            return param[0];

        }

    }
}
