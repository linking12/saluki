package com.quancheng.saluki.core.grpc.client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.grpc.MethodDescriptorUtils;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public class JavaProxyClient<T> extends AbstractProtocolClient<T> {

    public JavaProxyClient(Cache<String, Channel> channelCache, String protocol, Class<?> protocolClass,
                           Callable<Channel> channelCallable, int rpcTimeout, int callType){
        super(channelCache, protocol, protocolClass, channelCallable, rpcTimeout, callType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getClient() {
        return (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { getProtocolClzz() },
                                          new JavaProxyInvoker());
    }

    @Override
    protected MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> doCreateMethodDesc(Method method,
                                                                                          Object[] args) {
        return MethodDescriptorUtils.createMethodDescriptor(getProtocol(), method);
    }

    @Override
    protected Pair<GeneratedMessageV3, Class<?>> doProcessArgs(Method method, Object[] args) {
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
