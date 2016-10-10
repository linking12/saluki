package com.quancheng.saluki.core.grpc;

import java.lang.reflect.Method;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.MethodDescriptor;

public class MethodDescriptorFactory {

    private static class GrpcMethodDescFactoryHolder {

        private static final MethodDescriptorFactory INSTANCE = new MethodDescriptorFactory();
    }

    private MethodDescriptorFactory(){
    }

    public static final MethodDescriptorFactory getInstance() {
        return GrpcMethodDescFactoryHolder.INSTANCE;
    }

    public MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> getMethodDesc(ProtocolProxy<?> protocolProxy,
                                                                                  Method method, Object[] args) {
        MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> methodDescriptor = null;
        if (protocolProxy.isGeneric() && method.getName().equals("$invoke")) {
            String protocol = (String) args[0];
            String methodName = (String) args[1];
            String[] parameterTypes = (String[]) args[2];
            Object[] param = (Object[]) args[3];
            methodDescriptor = getGenericMethodDesc(protocol, methodName, parameterTypes, param);
        } else {
            methodDescriptor = getNormalMethodDesc(protocolProxy.getProtocol(), args, method);
        }
        return methodDescriptor;
    }

    private MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> getGenericMethodDesc(String protocol,
                                                                                          String methodName,
                                                                                          String[] parameterTypes,
                                                                                          Object[] param) {
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
        return GrpcUtils.createMethodDescriptor(protocol, methodName, paramType[0], paramType[1]);
    }

    private MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> getNormalMethodDesc(String protocol, Object[] args,
                                                                                         Method method) {
        if (args.length > 1) {
            throw new IllegalArgumentException("grpc not support multiple args,args is " + args + " length is "
                                               + args.length);
        }
        return GrpcUtils.createMethodDescriptor(protocol, method);
    }
}
