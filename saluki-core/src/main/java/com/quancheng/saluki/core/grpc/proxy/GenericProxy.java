package com.quancheng.saluki.core.grpc.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.grpc.MethodDescriptorUtils;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public class GenericProxy extends AbstractProtocolProxy<Object> {

    public GenericProxy(String protocol, Callable<Channel> channelCallable, int rpcTimeout, int callType,
                        boolean isGeneric){
        super(protocol, channelCallable, rpcTimeout, callType, isGeneric);
    }

    @Override
    public Object getProxy() {
        return Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { GenericService.class },
                                      new JavaProxyInvoker(true));
    }

    @Override
    protected MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> buildMethodDescriptor(Method method,
                                                                                             Object[] args) {
        String protocol = (String) args[0];
        String methodName = (String) args[1];
        String[] parameterTypes = (String[]) args[2];
        try {
            Class<?> requestType = ReflectUtil.name2class(parameterTypes[0]);
            Class<?> responseType = ReflectUtil.name2class(parameterTypes[1]);
            com.google.protobuf.GeneratedMessageV3 argsReq = MethodDescriptorUtils.buildDefautInstance(requestType);
            com.google.protobuf.GeneratedMessageV3 argsRep = MethodDescriptorUtils.buildDefautInstance(responseType);
            return MethodDescriptorUtils.createMethodDescriptor(protocol, methodName, argsReq, argsRep);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("grpc requestType and responseType must instanceof com.google.protobuf.GeneratedMessageV3");
        }
    }
}
