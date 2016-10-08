package com.quancheng.saluki.core.grpc;

import java.lang.reflect.Method;

import com.quancheng.saluki.core.utils.ReflectUtil;

public class GrpcUtils {

    public static io.grpc.MethodDescriptor<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> createMethodDescriptor(Class<?> clzz,
                                                                                                                                                  Method method) {

        String clzzName = clzz.getName();
        return createMethodDescriptor(clzzName, method);
    }

    public static io.grpc.MethodDescriptor<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> createMethodDescriptor(String clzzName,
                                                                                                                                                  Method method) {
        String methodName = method.getName();
        com.google.protobuf.GeneratedMessageV3 argsReq = (com.google.protobuf.GeneratedMessageV3) ReflectUtil.newMethodReq(method);
        com.google.protobuf.GeneratedMessageV3 argsRep = (com.google.protobuf.GeneratedMessageV3) ReflectUtil.newMethodRep(method);
        return createMethodDescriptor(clzzName, methodName, argsReq, argsRep);
    }

    public static io.grpc.MethodDescriptor<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> createMethodDescriptor(String clzzName,
                                                                                                                                                  String methodName,
                                                                                                                                                  com.google.protobuf.GeneratedMessageV3 argsReq,
                                                                                                                                                  com.google.protobuf.GeneratedMessageV3 argsRep) {
        return io.grpc.MethodDescriptor.create(io.grpc.MethodDescriptor.MethodType.UNARY,
                                               generateFullMethodName(clzzName, methodName),
                                               io.grpc.protobuf.ProtoUtils.marshaller(argsReq),
                                               io.grpc.protobuf.ProtoUtils.marshaller(argsRep));
    }

    public static String generateFullMethodName(String protocolName, String methodName) {
        return io.grpc.MethodDescriptor.generateFullMethodName(protocolName, methodName);
    }

    public static String generateServiceName(Class<?> protocol) {
        return protocol.getName();
    }
}
