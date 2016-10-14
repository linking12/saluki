package com.quancheng.saluki.core.grpc.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.grpc.MethodDescriptorUtils;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.ReflectUtil;

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
                                          new JavaProxyInvoker());
    }

    @Override
    protected MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> buildMethodDescriptor(Method method,
                                                                                             Object[] args) {
        return MethodDescriptorUtils.createMethodDescriptor(getProtocol(), method);
    }

    @Override
    protected Pair<GeneratedMessageV3, Class<?>> processParam(Method method, Object[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException("grpc not support multiple args,args is " + args + " length is "
                                               + args.length);
        }
        args = new Object[] { MethodDescriptorUtils.convertPojoToPbModel(args[0]) };
        GeneratedMessageV3 arg = (GeneratedMessageV3) args[0];
        Class<?> returnType = ReflectUtil.getTypeRep(method);
        return new ImmutablePair<GeneratedMessageV3, Class<?>>(arg, returnType);
    }

}
