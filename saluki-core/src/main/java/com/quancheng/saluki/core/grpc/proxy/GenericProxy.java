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
                                      new JavaProxyInvoker());
    }

    @Override
    protected MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> buildMethodDescriptor(Method method,
                                                                                             Object[] args) {
        String protocol = (String) args[0];
        String methodName = (String) args[1];
        String[] parameterTypes = (String[]) args[2];
        Object[] param = (Object[]) args[3];
        if (parameterTypes.length != 2) {
            throw new IllegalArgumentException("generic call,the request and response type must be set "
                                               + parameterTypes + " length is " + parameterTypes.length);
        }
        if (param.length > 1) {
            throw new IllegalArgumentException("grpc not support multiple args,args is " + param + " length is "
                                               + param.length);
        }
        GeneratedMessageV3[] paramType = new GeneratedMessageV3[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            String parameterTypeStr = parameterTypes[i];
            try {
                Class<?> parameterType = ReflectUtil.name2class(parameterTypeStr);
                if (GeneratedMessageV3.class.isAssignableFrom(parameterType)) {
                    Object obj = ReflectUtil.classInstance(parameterType);
                    paramType[i] = (GeneratedMessageV3) obj;
                } else {
                    throw new IllegalArgumentException("grpc paramter must instanceof com.google.protobuf.GeneratedMessageV3"
                                                       + " but the type is " + parameterType);
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("not found paramter in classpath, " + " but the type is "
                                                   + parameterTypeStr);
            }
        }
        return MethodDescriptorUtils.createMethodDescriptor(protocol, methodName, paramType[0], paramType[1]);
    }
}
