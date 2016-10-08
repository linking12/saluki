package com.quancheng.saluki.core.grpc;

import java.lang.reflect.Method;

import com.quancheng.saluki.core.utils.ReflectUtil;

public class GrpcUtils {

    public static io.grpc.MethodDescriptor<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> createMethodDescriptor(Class<?> clzz,
                                                                                                                                                  Method method) {
        com.google.protobuf.GeneratedMessageV3 argsReq = (com.google.protobuf.GeneratedMessageV3) ReflectUtil.newMethodReq(method);
        com.google.protobuf.GeneratedMessageV3 argsRep = (com.google.protobuf.GeneratedMessageV3) ReflectUtil.newMethodRep(method);
        return io.grpc.MethodDescriptor.create(io.grpc.MethodDescriptor.MethodType.UNARY,
                                               generateFullMethodName(clzz, method.getName()),
                                               io.grpc.protobuf.ProtoUtils.marshaller(argsReq),
                                               io.grpc.protobuf.ProtoUtils.marshaller(argsRep));
    }

    private static String generateFullMethodName(Class<?> protocol, String methodName) {
        return io.grpc.MethodDescriptor.generateFullMethodName(protocol.getName(), methodName);
    }
}
