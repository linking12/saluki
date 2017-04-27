/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.client.GrpcAsyncCall;
import com.quancheng.saluki.core.grpc.client.GrpcRequest;
import com.quancheng.saluki.core.grpc.client.GrpcResponse;
import com.quancheng.saluki.core.grpc.client.async.RetryOptions;
import com.quancheng.saluki.core.grpc.client.hystrix.GrpcBlockingUnaryCommand;
import com.quancheng.saluki.core.grpc.client.hystrix.GrpcFutureUnaryCommand;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.grpc.service.ClientServerMonitor;
import com.quancheng.saluki.core.grpc.service.MonitorService;
import com.quancheng.saluki.core.grpc.util.GrpcReflectUtil;
import com.quancheng.saluki.serializer.exception.ProtobufException;

import io.grpc.Attributes;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;

/**
 * @author shimingliu 2016年12月14日 下午9:38:34
 * @version AbstractClientInvocation.java, v 0.0.1 2016年12月14日 下午9:38:34 shimingliu
 */
public abstract class AbstractClientInvocation implements InvocationHandler {

    private static final Logger                        log         = LoggerFactory.getLogger(AbstractClientInvocation.class);

    private final Map<String, Integer>                 methodRetries;

    private final ConcurrentMap<String, AtomicInteger> concurrents = Maps.newConcurrentMap();

    private volatile ClientServerMonitor               clientServerMonitor;

    private volatile GrpcURL                           refUrl;

    private volatile Attributes                        attributes;

    public AbstractClientInvocation(Map<String, Integer> methodRetries){
        this.methodRetries = methodRetries;
    }

    protected abstract GrpcRequest buildGrpcRequest(Method method, Object[] args);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (GrpcReflectUtil.isToStringMethod(method)) {
            return AbstractClientInvocation.this.toString();
        }
        GrpcRequest request = buildGrpcRequest(method, args);
        refUrl = request.getRefUrl();
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
        Attributes attributes = this.buildAttributes(refUrl);
        GrpcAsyncCall grpcAsyncCall = GrpcAsyncCall.createGrpcAsyncCall(channel, retryConfig, attributes);
        long start = System.currentTimeMillis();
        getConcurrent(serviceName, methodName).incrementAndGet();
        try {
            reqProtoBufer = request.getRequestArg();
            switch (request.getMethodRequest().getCallType()) {
                case Constants.RPCTYPE_ASYNC:
                    respProtoBufer = new GrpcFutureUnaryCommand(grpcAsyncCall, refUrl, methodDesc, reqProtoBufer,
                                                                timeOut).execute();
                    break;
                case Constants.RPCTYPE_BLOCKING:
                    respProtoBufer = new GrpcBlockingUnaryCommand(grpcAsyncCall, refUrl, methodDesc,
                                                                  reqProtoBufer).execute();
                    break;
                default:
                    respProtoBufer = new GrpcFutureUnaryCommand(grpcAsyncCall, refUrl, methodDesc, reqProtoBufer,
                                                                timeOut).execute();
                    break;
            }
            Class<?> respPojoType = request.getMethodRequest().getResponseType();
            GrpcResponse response = new GrpcResponse.Default(respProtoBufer, respPojoType);
            Object respPojo = response.getResponseArg();
            collect(serviceName, methodName, reqProtoBufer, respProtoBufer, start, false);
            return respPojo;
        } catch (ProtobufException e) {
            collect(serviceName, methodName, reqProtoBufer, respProtoBufer, start, true);
            RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
            throw rpcFramwork;
        } finally {
            log.info(String.format("Service: %s  Method: %s  RemoteAddress: %s", serviceName, methodName,
                                   getProviderServer()));
            request.returnChannel(channel);
            getConcurrent(serviceName, methodName).decrementAndGet();
        }
    }

    public InetSocketAddress getProviderServer() {
        InetSocketAddress currentServer = (InetSocketAddress) attributes.get(GrpcAsyncCall.CURRENT_ADDR_KEY);
        RpcContext.getContext().setAttachment(Constants.REMOTE_ADDRESS, String.valueOf(currentServer));
        return currentServer;
    }

    private Attributes buildAttributes(GrpcURL url) {
        Attributes attributes = Attributes.newBuilder().set(GrpcAsyncCall.GRPC_REF_URL, url).build();
        this.attributes = attributes;
        return attributes;
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

    private void collect(String serviceName, String methodName, Message request, Message response, long start,
                         boolean error) {
        try {
            String provider = getProviderServer().getHostName();
            if (request == null || response == null) {
                return;
            }
            long elapsed = System.currentTimeMillis() - start; // 计算调用耗时
            int concurrent = getConcurrent(serviceName, methodName).get(); // 当前并发数
            String service = serviceName; // 获取服务名称
            String method = methodName; // 获取方法名
            String host = refUrl.getHost();
            Integer port = refUrl.getPort();
            if (clientServerMonitor == null) {
                clientServerMonitor = new ClientServerMonitor(refUrl);
            }
            clientServerMonitor.collect(new GrpcURL(Constants.MONITOR_PROTOCOL, host, port, //
                                                    service + "/" + method, //
                                                    MonitorService.TIMESTAMP, String.valueOf(start), //
                                                    MonitorService.APPLICATION,
                                                    refUrl.getParameter(Constants.APPLICATION_NAME), //
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
            log.warn("Failed to monitor count service " + serviceName + ", cause: " + t.getMessage());
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
