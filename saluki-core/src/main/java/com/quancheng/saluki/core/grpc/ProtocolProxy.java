package com.quancheng.saluki.core.grpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.service.GenericService;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;

public final class ProtocolProxy<T> {

    private final String          protocol;

    private final ChannelCallable channelCallable;

    private final int             callType;

    private final int             rpcTimeout;

    private final boolean         isGeneric;

    private volatile Class<?>     protocolClzz;

    public static interface ChannelCallable {

        public Channel getGrpcChannel(String serviceInterface);
    }

    public ProtocolProxy(String protocol, ChannelCallable channelCallable, int rpcTimeout, int callType,
                         boolean isGeneric) throws ClassNotFoundException{
        this.protocol = protocol;
        this.channelCallable = channelCallable;
        this.rpcTimeout = rpcTimeout;
        this.callType = callType;
        this.isGeneric = isGeneric;
    }

    public Object getProxy() {
        // 如果是泛化调用，用GenericService代替接口
        if (isGeneric) {
            return this.getJavaProxy();
        } else {
            try {
                protocolClzz = ReflectUtil.name2class(protocol);
                boolean isInterface = Modifier.isInterface(protocolClzz.getModifiers());
                // 如果是接口的话，说明需要用动态代理
                if (isInterface) {
                    return this.getJavaProxy();
                } // 否则说明使用的是原生的grpc stub方式来调用
                else {
                    return this.getGrpcStub();
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("no class find in classpath", e);
            }
        }
    }

    private T getGrpcStub() {
        String clzName = this.protocol;
        if (StringUtils.contains(clzName, "$")) {
            try {
                String parentName = StringUtils.substringBefore(clzName, "$");
                Class<?> clzz = ReflectUtil.name2class(parentName);
                Method method;
                switch (callType) {
                    case SalukiConstants.RPCTYPE_ASYNC:
                        method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
                        break;
                    case SalukiConstants.RPCTYPE_BLOCKING:
                        method = clzz.getMethod("newBlockingStub", io.grpc.Channel.class);
                        break;
                    default:
                        method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
                        break;
                }
                @SuppressWarnings("unchecked")
                T value = (T) method.invoke(null, channelCallable.getGrpcChannel(clzName));
                return value;
            } catch (Exception e) {
                throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file", e);
            }
        } else {
            throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file");
        }
    }

    @SuppressWarnings("unchecked")
    private Object getJavaProxy() {
        Object proxy;
        if (this.isGeneric) {
            proxy = Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { GenericService.class },
                                           new JavaProxyInvoker());
        } else {
            proxy = (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { protocolClzz },
                                               new JavaProxyInvoker());
        }
        return proxy;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getRpcTimeout() {
        return rpcTimeout;
    }

    public boolean isGeneric() {
        return isGeneric;
    }

    private class JavaProxyInvoker implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            if ("toString".equals(methodName) && parameterTypes.length == 0) {
                return proxy.toString();
            }
            if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
                return proxy.hashCode();
            }
            if ("equals".equals(methodName) && parameterTypes.length == 1) {
                return proxy.equals(args[0]);
            }

            GrpcMethodDescFactory factory = GrpcMethodDescFactory.getInstance();
            MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> methodDescriptor = factory.getMethodDesc(ProtocolProxy.this,
                                                                                                              method,
                                                                                                              args);
            // 回调获取通道对象，此处有缓存，缓存1000个不同方法所对应的通道
            Channel chanel = channelCallable.getGrpcChannel(protocol);
            ClientCall<GeneratedMessageV3, GeneratedMessageV3> newCall = chanel.newCall(methodDescriptor,
                                                                                        CallOptions.DEFAULT);
            com.google.protobuf.GeneratedMessageV3 arg = (com.google.protobuf.GeneratedMessageV3) args[0];
            switch (callType) {
                case SalukiConstants.RPCTYPE_ASYNC:
                    return ClientCalls.futureUnaryCall(newCall, arg).get(rpcTimeout, TimeUnit.SECONDS);
                case SalukiConstants.RPCTYPE_BLOCKING:
                    return ClientCalls.blockingUnaryCall(newCall, arg);
                default:
                    return ClientCalls.futureUnaryCall(newCall, arg).get(rpcTimeout, TimeUnit.SECONDS);
            }
        }
    }
}
