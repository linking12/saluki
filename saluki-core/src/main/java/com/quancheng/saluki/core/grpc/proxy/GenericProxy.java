package com.quancheng.saluki.core.grpc.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.grpc.MethodDescriptorUtils;
import com.quancheng.saluki.core.grpc.SalukiClassLoader;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public class GenericProxy extends AbstractProtocolProxy<Object> {

    private SalukiClassLoader classLoader;

    public GenericProxy(String protocol, Callable<Channel> channelCallable, int rpcTimeout, int callType,
                        boolean isGeneric){
        super(protocol, channelCallable, rpcTimeout, callType);
    }

    public void setSalukiClassLoader(SalukiClassLoader classLoader) {
        this.classLoader = classLoader;
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
        Class<?> requestType;
        Class<?> responseType;
        try {
            requestType = ReflectUtil.name2class(parameterTypes[0]);
        } catch (ClassNotFoundException e) {
            requestType = doLoadClass(parameterTypes[0]);
        }
        try {
            responseType = ReflectUtil.name2class(parameterTypes[1]);
        } catch (ClassNotFoundException e) {
            responseType = doLoadClass(parameterTypes[1]);
        }
        com.google.protobuf.GeneratedMessageV3 argsReq = MethodDescriptorUtils.buildDefautInstance(requestType);
        com.google.protobuf.GeneratedMessageV3 argsRep = MethodDescriptorUtils.buildDefautInstance(responseType);
        return MethodDescriptorUtils.createMethodDescriptor(protocol, methodName, argsReq, argsRep);
    }

    private Class<?> doLoadClass(String className) {
        try {
            classLoader.addClassPath();
            return classLoader.loadClass(className);
        } catch (Exception e) {
            throw new IllegalArgumentException("grpc  responseType must instanceof com.google.protobuf.GeneratedMessageV3",
                                               new ClassNotFoundException("Class " + className + " not found"));
        }

    }

}
