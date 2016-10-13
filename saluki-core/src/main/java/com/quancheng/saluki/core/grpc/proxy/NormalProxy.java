package com.quancheng.saluki.core.grpc.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.grpc.MethodDescriptorUtils;
import com.quancheng.saluki.core.utils.ClassHelper;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public class NormalProxy<T> extends AbstractProtocolProxy<T> {

    public NormalProxy(String protocol, Class<?> protocolClass, Callable<Channel> channelCallable, int rpcTimeout,
                       int callType){
        super(protocol, protocolClass, channelCallable, rpcTimeout, callType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getProxy() {
        return (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { getProtocolClzz() },
                                          new JavaProxyInvoker(false));
    }

    @Override
    protected MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> buildMethodDescriptor(Method method,
                                                                                             Object[] args) {
        return MethodDescriptorUtils.createMethodDescriptor(getProtocol(), method);
    }

}
