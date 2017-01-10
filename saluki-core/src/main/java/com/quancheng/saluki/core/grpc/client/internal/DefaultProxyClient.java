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
import java.util.Map;

import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.client.GrpcProtocolClient;
import com.quancheng.saluki.core.grpc.client.GrpcRequest;
import com.quancheng.saluki.core.grpc.util.GrpcReflectUtil;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.ReflectUtils;

/**
 * @author shimingliu 2016年12月14日 下午9:35:56
 * @version ProxyClient.java, v 0.0.1 2016年12月14日 下午9:35:56 shimingliu
 */
public class DefaultProxyClient<T> implements GrpcProtocolClient<T> {

    private final Map<String, Integer> methodRetries;

    private final String               interfaceName;

    private final Class<?>             interfaceClass;

    private final GrpcURL              refUrl;

    public DefaultProxyClient(String interfaceName, Map<String, Integer> methodRetries, GrpcURL refUrl){
        this.interfaceName = interfaceName;
        try {
            this.interfaceClass = ReflectUtils.name2class(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        this.methodRetries = methodRetries;
        this.refUrl = refUrl;
    }

    public String getFullServiceName() {
        return this.interfaceName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getGrpcClient(GrpcProtocolClient.ChannelPool channelPoll, int callType, int callTimeout) {
        return (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { interfaceClass },
                                          new DefaultProxyClientInvocation(channelPoll, callType, callTimeout));
    }

    private class DefaultProxyClientInvocation extends AbstractClientInvocation {

        private final GrpcProtocolClient.ChannelPool channelPool;
        private final int                            callType;
        private final int                            callTimeout;

        public DefaultProxyClientInvocation(GrpcProtocolClient.ChannelPool call, int callType, int callTimeout){
            super(DefaultProxyClient.this.methodRetries);
            this.channelPool = call;
            this.callType = callType;
            this.callTimeout = callTimeout;
        }

        @Override
        protected GrpcRequest buildGrpcRequest(Method method, Object[] args) {
            boolean isLegalMethod = GrpcReflectUtil.isLegal(method);
            if (isLegalMethod) {
                throw new IllegalArgumentException("remote call type do not support this method " + method.getName());
            }
            if (args.length != 1) {
                throw new IllegalArgumentException("grpc not support multiple args,args is " + args + " length is "
                                                   + args.length);
            }
            Object arg = args[0];
            GrpcRequest request = new GrpcRequest.Default(DefaultProxyClient.this.refUrl, channelPool);
            GrpcRequest.MethodRequest methodRequest = new GrpcRequest.MethodRequest(method.getName(), arg.getClass(),
                                                                                    method.getReturnType(), arg,
                                                                                    callType, callTimeout);
            request.setMethodRequest(methodRequest);
            return request;
        }

    }

}
