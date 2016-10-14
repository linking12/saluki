package com.quancheng.saluki.core.grpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.grpc.MethodDescriptorUtils;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;

public abstract class AbstractProtocolProxy<T> implements ProtocolProxy<T> {

    private final String                 protocol;

    private final Callable<Channel>      channelCallable;

    private final int                    callType;

    private final int                    rpcTimeout;

    private final Class<?>               protocolClzz;

    private final Cache<String, Channel> channelCache;

    protected class JavaProxyInvoker implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            if ("toString".equals(methodName) && parameterTypes.length == 0) {
                return AbstractProtocolProxy.this.toString();
            }
            if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
                return AbstractProtocolProxy.this.hashCode();
            }
            if ("equals".equals(methodName) && parameterTypes.length == 1) {
                return AbstractProtocolProxy.this.equals(args[0]);
            }
            try {
                Pair<GeneratedMessageV3, Class<?>> pairParam = processParam(method, args);
                GeneratedMessageV3 arg = pairParam.getLeft();
                Class<?> returnType = pairParam.getRight();
                ClientCall<GeneratedMessageV3, GeneratedMessageV3> newCall = getChannel().newCall(buildMethodDescriptor(method,
                                                                                                                        args),
                                                                                                  CallOptions.DEFAULT);
                GeneratedMessageV3 response = null;
                switch (callType) {
                    case SalukiConstants.RPCTYPE_ASYNC:
                        response = ClientCalls.futureUnaryCall(newCall, arg).get(rpcTimeout, TimeUnit.SECONDS);
                        break;
                    case SalukiConstants.RPCTYPE_BLOCKING:
                        response = ClientCalls.blockingUnaryCall(newCall, arg);
                        break;
                    default:
                        response = ClientCalls.futureUnaryCall(newCall, arg).get(rpcTimeout, TimeUnit.SECONDS);
                        break;
                }
                return MethodDescriptorUtils.convertPbModelToPojo(response, returnType);
            } catch (Throwable e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
    }

    protected abstract Pair<GeneratedMessageV3, Class<?>> processParam(Method method, Object[] args) throws Throwable;

    protected abstract MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> buildMethodDescriptor(Method method,
                                                                                                      Object[] args);

    public AbstractProtocolProxy(String protocol, Class<?> protocolClass, Callable<Channel> channelCallable,
                                 int rpcTimeout, int callType){
        this.protocol = protocol;
        this.channelCallable = channelCallable;
        this.rpcTimeout = rpcTimeout;
        this.callType = callType;
        this.protocolClzz = protocolClass;
        this.channelCache = CacheBuilder.newBuilder()//
                                        .maximumSize(5000L)//
                                        .softValues()//
                                        .ticker(Ticker.systemTicker())//
                                        .build();
    }

    public Channel getChannel() {
        try {
            return channelCache.get(protocol, channelCallable);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String getProtocol() {
        return protocol;
    }

    public Callable<Channel> getChannelCallable() {
        return channelCallable;
    }

    public int getCallType() {
        return callType;
    }

    public int getRpcTimeout() {
        return rpcTimeout;
    }

    public Class<?> getProtocolClzz() {
        return protocolClzz;
    }

    public Cache<String, Channel> getChannelCache() {
        return channelCache;
    }
}
