/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.ypp.thrall.core.grpc.client.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.quancheng.saluki.serializer.exception.ProtobufException;
import com.ypp.thrall.core.common.Constants;
import com.ypp.thrall.core.common.ThrallURL;
import com.ypp.thrall.core.grpc.client.GrpcAsyncCall;
import com.ypp.thrall.core.grpc.client.GrpcRequest;
import com.ypp.thrall.core.grpc.client.GrpcResponse;
import com.ypp.thrall.core.grpc.client.async.RetryOptions;
import com.ypp.thrall.core.grpc.exception.RpcErrorMsgConstant;
import com.ypp.thrall.core.grpc.exception.RpcFrameworkException;
import com.ypp.thrall.core.grpc.exception.RpcServiceException;
import com.ypp.thrall.core.grpc.service.ClientServerMonitor;
import com.ypp.thrall.core.grpc.service.MonitorService;
import com.ypp.thrall.core.grpc.util.GrpcReflectUtil;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

/**
 * @author shimingliu 2016年12月14日 下午9:38:34
 * @version AbstractClientInvocation.java, v 0.0.1 2016年12月14日 下午9:38:34 shimingliu
 */
public abstract class AbstractClientInvocation implements InvocationHandler {

    private static final Logger                        log         = LoggerFactory.getLogger(AbstractClientInvocation.class);

    private final MonitorService                       clientServerMonitor;

    private final Map<String, Integer>                 methodRetries;

    private final ConcurrentMap<String, AtomicInteger> concurrents = new ConcurrentHashMap<String, AtomicInteger>();

    public AbstractClientInvocation(Map<String, Integer> methodRetries){
        this.clientServerMonitor = new ClientServerMonitor(getSourceRefUrl());
        this.methodRetries = methodRetries;
    }

    protected abstract ThrallURL getSourceRefUrl();

    protected abstract GrpcRequest buildGrpcRequest(Method method, Object[] args);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (GrpcReflectUtil.isToStringMethod(method)) {
            return AbstractClientInvocation.this.toString();
        }
        GrpcRequest request = buildGrpcRequest(method, args);
        // 准备Grpc参数begin
        String serviceName = request.getServiceName();
        String methodName = request.getMethodRequest().getMethodName();
        Message reqProtoBufer = null;
        Message respProtoBufer = null;
        MethodDescriptor<Message, Message> methodDesc = request.getMethodDescriptor();
        int timeOut = request.getMethodRequest().getCallTimeout();
        // 准备Grpc调用参数end
        Channel channel = request.getChannel();
        RetryOptions retryConfig = createRetryOption(methodName);
        GrpcAsyncCall grpcAsyncCall = GrpcAsyncCall.createGrpcAsyncCall(channel, retryConfig);
        long start = System.currentTimeMillis();
        getConcurrent(serviceName, methodName).incrementAndGet();
        try {
            reqProtoBufer = request.getRequestArg();
            switch (request.getMethodRequest().getCallType()) {
                case Constants.RPCTYPE_ASYNC:
                    respProtoBufer = grpcAsyncCall.unaryFuture(reqProtoBufer, methodDesc).get(timeOut,
                                                                                              TimeUnit.MILLISECONDS);
                    break;
                case Constants.RPCTYPE_BLOCKING:
                    respProtoBufer = grpcAsyncCall.blockingUnaryResult(reqProtoBufer, methodDesc);
                    break;
                default:
                    respProtoBufer = grpcAsyncCall.unaryFuture(reqProtoBufer, methodDesc).get(timeOut,
                                                                                              TimeUnit.MILLISECONDS);
                    break;
            }
            Class<?> respPojoType = request.getMethodRequest().getResponseType();
            GrpcResponse response = new GrpcResponse.Default(respProtoBufer, respPojoType);
            Object respPojo = response.getResponseArg();
            collect(serviceName, methodName, reqProtoBufer, respProtoBufer, grpcAsyncCall.getRemoteAddress(), start,
                    false);
            return respPojo;
        } catch (ProtobufException | InterruptedException | ExecutionException | TimeoutException e) {
            collect(serviceName, methodName, reqProtoBufer, respProtoBufer, grpcAsyncCall.getRemoteAddress(), start,
                    true);
            if (e instanceof ProtobufException) {
                RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
                throw rpcFramwork;
            } else if (e instanceof TimeoutException) {
                RpcServiceException rpcService = new RpcServiceException(e, RpcErrorMsgConstant.SERVICE_TIMEOUT);
                throw rpcService;
            } else {
                RpcServiceException rpcService = new RpcServiceException(e);
                throw rpcService;
            }
        } finally {
            request.returnChannel(channel);
            getConcurrent(serviceName, methodName).decrementAndGet();
        }
    }

    private RetryOptions createRetryOption(String methodName) {
        if (methodRetries.size() == 1 && methodRetries.containsKey("*")) {
            Integer retries = methodRetries.get("*");
            return new RetryOptions(retries, true);
        } else {
            Integer retries = methodRetries.get(methodName);
            if (retries != null) {
                return new RetryOptions(retries, true);
            } else {
                return new RetryOptions(0, false);
            }
        }
    }

    private void collect(String serviceName, String methodName, Message request, Message response,
                         SocketAddress remoteAddress, long start, boolean error) {
        try {
            if (request == null || response == null) {
                return;
            }
            long elapsed = System.currentTimeMillis() - start; // 计算调用耗时
            int concurrent = getConcurrent(serviceName, methodName).get(); // 当前并发数
            String service = serviceName; // 获取服务名称
            String method = methodName; // 获取方法名
            InetSocketAddress remote = (InetSocketAddress) remoteAddress;
            String provider = remote.getHostName();// 服务端主机
            String host = getSourceRefUrl().getHost();
            Integer port = getSourceRefUrl().getPort();
            clientServerMonitor.collect(new ThrallURL(Constants.MONITOR_PROTOCOL, host, port, //
                                                      service + "/" + method, //
                                                      MonitorService.TIMESTAMP, String.valueOf(start), //
                                                      MonitorService.APPLICATION,
                                                      getSourceRefUrl().getParameter(Constants.APPLICATION_NAME), //
                                                      MonitorService.INTERFACE, service, //
                                                      MonitorService.METHOD, method, //
                                                      MonitorService.PROVIDER, provider, //
                                                      error ? MonitorService.FAILURE : MonitorService.SUCCESS, "1", //
                                                      MonitorService.ELAPSED, String.valueOf(elapsed), //
                                                      MonitorService.CONCURRENT, String.valueOf(concurrent), //
                                                      MonitorService.INPUT, String.valueOf(request.getSerializedSize()), //
                                                      MonitorService.OUTPUT,
                                                      String.valueOf(response.getSerializedSize())));
        } catch (Throwable t) {
            log.error("Failed to monitor count service " + serviceName + ", cause: " + t.getMessage(), t);
        }
    }

    private AtomicInteger getConcurrent(String servcieName, String methodName) {
        String key = servcieName + "." + methodName;
        AtomicInteger concurrent = concurrents.get(key);
        if (concurrent == null) {
            concurrents.putIfAbsent(key, new AtomicInteger());
            concurrent = concurrents.get(key);
        }
        return concurrent;
    }
}
