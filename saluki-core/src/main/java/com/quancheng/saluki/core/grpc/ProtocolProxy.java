package com.quancheng.saluki.core.grpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;

public final class ProtocolProxy<T> {

    private final Class<T> protocol;

    private final Channel  channel;

    private final int      callType;

    private final int      rpcTimeout;

    public ProtocolProxy(String interfaceName, Channel channel, int rpcTimeout,
                         int callType) throws ClassNotFoundException{
        this.protocol = (Class<T>) Class.forName(interfaceName);
        this.channel = channel;
        this.rpcTimeout = rpcTimeout;
        this.callType = callType;
    }

    public T getProxy() {
        boolean isInterface = Modifier.isInterface(this.protocol.getModifiers());
        if (isInterface) {
            return this.getJavaProxy();
        } else {
            return this.getGrpcStub();
        }

    }

    private T getGrpcStub() {
        String clzName = this.protocol.getName();
        if (StringUtils.contains(clzName, "$")) {
            try {
                String parentName = StringUtils.substringBefore(clzName, "$");
                Class clzz = Class.forName(parentName);
                Method method;
                switch (callType) {
                    case 1:
                        method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
                        break;
                    case 2:
                        method = clzz.getMethod("newBlockingStub", io.grpc.Channel.class);
                        break;
                    default:
                        method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
                        break;
                }
                T value = (T) method.invoke(null, channel);
                return value;
            } catch (Exception e) {
                throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file", e);
            }
        } else {
            throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file");
        }
    }

    private T getJavaProxy() {
        T proxy = (T) Proxy.newProxyInstance(protocol.getClassLoader(), new Class[] { protocol },
                                             new JavaProxyInvoker());
        return proxy;
    }

    private class JavaProxyInvoker implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args.length > 1) {
                throw new IllegalArgumentException("grpc not support multiple args,args is " + args + " length is "
                                                   + args.length);
            }
            io.grpc.MethodDescriptor<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> methodDescriptor = GrpcUtils.createMethodDescriptor(protocol,
                                                                                                                                                                         method);
            ClientCall<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> newCall = channel.newCall(methodDescriptor,
                                                                                                                                 CallOptions.DEFAULT);
            com.google.protobuf.GeneratedMessageV3 arg = (com.google.protobuf.GeneratedMessageV3) args[0];
            switch (callType) {
                case 1:
                    return ClientCalls.futureUnaryCall(newCall, arg).get(rpcTimeout, TimeUnit.SECONDS);
                case 2:
                    return ClientCalls.blockingUnaryCall(newCall, arg);
                default:
                    return ClientCalls.futureUnaryCall(newCall, arg).get(rpcTimeout, TimeUnit.SECONDS);
            }
        }

    }

}
