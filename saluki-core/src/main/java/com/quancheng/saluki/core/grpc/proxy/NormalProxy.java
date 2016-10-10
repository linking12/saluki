package com.quancheng.saluki.core.grpc.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.grpc.GrpcUtils;
import com.quancheng.saluki.core.utils.ClassHelper;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public class NormalProxy<T> extends AbstractProtocolProxy<T> {

    public NormalProxy(String protocol, Callable<Channel> channelCallable, int rpcTimeout, int callType,
                       boolean isGeneric){
        super(protocol, channelCallable, rpcTimeout, callType, isGeneric);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getProxy() {
        return (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { getProtocolClzz() },
                                          new JavaProxyInvoker());
    }

    @Override
    protected MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> buildMethodDescriptor(Method method,
                                                                                             Object[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException("grpc not support multiple args,args is " + args + " length is "
                                               + args.length);
        }
        return GrpcUtils.createMethodDescriptor(getProtocol(), method);
    }

}
