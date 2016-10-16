package com.quancheng.saluki.core.grpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.grpc.MethodDescriptorUtils;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;

public abstract class AbstractProtocolClient<T> implements ProtocolClient<T> {

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
                return AbstractProtocolClient.this.toString();
            }
            if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
                return AbstractProtocolClient.this.hashCode();
            }
            if ("equals".equals(methodName) && parameterTypes.length == 1) {
                return AbstractProtocolClient.this.equals(args[0]);
            }
            try {
                Pair<GeneratedMessageV3, Class<?>> pairParam = AbstractProtocolClient.this.doProcessArgs(method, args);
                GeneratedMessageV3 arg = pairParam.getLeft();
                Class<?> returnType = pairParam.getRight();
                MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> methodDesc = AbstractProtocolClient.this.doCreateMethodDesc(method,
                                                                                                                                     args);
                ClientCall<GeneratedMessageV3, GeneratedMessageV3> newCall = AbstractProtocolClient.this.getChannel().newCall(methodDesc,
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

    protected abstract Pair<GeneratedMessageV3, Class<?>> doProcessArgs(Method method, Object[] args) throws Throwable;

    protected abstract MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> doCreateMethodDesc(Method method,
                                                                                                   Object[] args);

    public AbstractProtocolClient(Cache<String, Channel> channelCache, String protocol, Class<?> protocolClass,
                                  Callable<Channel> channelCallable, int rpcTimeout, int callType){
        this.protocol = protocol;
        this.channelCallable = channelCallable;
        this.rpcTimeout = rpcTimeout;
        this.callType = callType;
        this.protocolClzz = protocolClass;
        this.channelCache = channelCache;
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
