package com.quancheng.saluki.core.grpc.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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

    public GenericProxy(String protocol, Class<?> protocolClass, Callable<Channel> channelCallable, int rpcTimeout,
                        int callType){
        super(protocol, protocolClass, channelCallable, rpcTimeout, callType);
    }

    public void setSalukiClassLoader(SalukiClassLoader classLoader) {
        this.classLoader = classLoader;
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

    @Override
    protected Pair<GeneratedMessageV3, Class<?>> processParam(Method method, Object[] args) throws Throwable {
        int length = ((String[]) args[2]).length;
        if (length != 2) {
            throw new IllegalArgumentException("generic call request type and response type must transmit"
                                               + " but length is  " + length);
        }
        Class<?> returnType = ReflectUtil.name2class(((String[]) args[2])[1]);
        String requestType = MethodDescriptorUtils.covertPojoTypeToPbModelType(((String[]) args[2])[0]);
        String responseType = MethodDescriptorUtils.covertPojoTypeToPbModelType(((String[]) args[2])[1]);
        args[2] = new String[] { requestType, responseType };
        Object[] param = (Object[]) args[3];
        if (param.length > 1) {
            throw new IllegalArgumentException("grpc call not support multiple args,args is " + param + " length is "
                                               + param.length);
        }
        args[3] = new Object[] { MethodDescriptorUtils.convertPojoToPbModel(param[0]) };
        GeneratedMessageV3 arg = (GeneratedMessageV3) ((Object[]) args[3])[0];
        return new ImmutablePair<GeneratedMessageV3, Class<?>>(arg, returnType);
    }

}
