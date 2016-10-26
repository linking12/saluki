package com.quancheng.saluki.core.grpc.client.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.grpc.filter.Filter;
import com.quancheng.saluki.core.grpc.filter.GrpcRequest;
import com.quancheng.saluki.core.grpc.filter.GrpcResponse;
import com.quancheng.saluki.core.grpc.server.support.ServerInvocation;
import com.quancheng.saluki.core.utils.ClassHelper;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;

/**
 * <strong>描述：</strong>TODO 描述 <br>
 * <strong>功能：</strong><br>
 * <strong>使用场景：</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author shimingliu 2016年10月18日 下午11:20:15
 * @version $Id: AbstractClientInvocation.java, v 0.0.1 2016年10月18日 下午11:20:15 shimingliu Exp $
 */
public abstract class AbstractClientInvocation implements InvocationHandler {

    private static final Logger          log = LoggerFactory.getLogger(InvocationHandler.class);

    private final List<Filter>           filters;

    private final Cache<String, Channel> channelCache;

    public AbstractClientInvocation(){
        this.filters = this.doInnerFilter();
        this.channelCache = CacheBuilder.newBuilder()//
                                        .maximumSize(10L)//
                                        .softValues()//
                                        .ticker(Ticker.systemTicker())//
                                        .build();
    }

    private Channel getChannel(GrpcRequest request) {
        try {
            return channelCache.get(request.getServiceName(), new Callable<Channel>() {

                @Override
                public Channel call() throws Exception {
                    return request.getChannel();
                }
            });
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    protected abstract GrpcRequest buildGrpcRequest(Method method, Object[] args);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return AbstractClientInvocation.this.toString();
        }
        GrpcRequest salukiRequest = this.buildGrpcRequest(method, args);
        for (Filter filter : filters) {
            filter.before(salukiRequest);
        }
        log.info("begin to call grpc service: " + salukiRequest.getServiceName() + ",request:"
                 + salukiRequest.getRequestArg().toString());
        ClientCall<Message, Message> newCall = getChannel(salukiRequest).newCall(salukiRequest.getMethodDescriptor(),
                                                                                 CallOptions.DEFAULT);
        Message resp = null;
        switch (salukiRequest.getMethodRequest().getCallType()) {
            case SalukiConstants.RPCTYPE_ASYNC:
                resp = ClientCalls.futureUnaryCall(newCall, salukiRequest.getRequestArg()).get(
                                                                                               salukiRequest.getMethodRequest().getCallTimeout(),
                                                                                               TimeUnit.SECONDS);
                break;
            case SalukiConstants.RPCTYPE_BLOCKING:
                resp = ClientCalls.blockingUnaryCall(newCall, salukiRequest.getRequestArg());
                break;
            default:
                resp = ClientCalls.futureUnaryCall(newCall, salukiRequest.getRequestArg()).get(
                                                                                               salukiRequest.getMethodRequest().getCallTimeout(),
                                                                                               TimeUnit.SECONDS);
                break;
        }
        log.info("after to call grpc service: " + salukiRequest.getServiceName() + ",response:" + resp.toString());
        GrpcResponse response = new GrpcResponse.Default(resp, salukiRequest.getMethodRequest().getResponseType());
        for (Filter filter : filters) {
            filter.after(response);
        }
        return response.getResponseArg();
    }

    private List<Filter> doInnerFilter() {
        Iterable<Filter> candidates = ServiceLoader.load(Filter.class, ClassHelper.getClassLoader());
        List<Filter> list = Lists.newArrayList();
        for (Filter current : candidates) {
            list.add(current);
        }
        return list;
    }

}
