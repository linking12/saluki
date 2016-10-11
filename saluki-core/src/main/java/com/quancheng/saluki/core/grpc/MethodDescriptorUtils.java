package com.quancheng.saluki.core.grpc;

import java.lang.reflect.Method;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.utils.ReflectUtil;
import com.quancheng.saluki.serializer.ProtobufEntity;

public class MethodDescriptorUtils {

    public static io.grpc.MethodDescriptor<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> createMethodDescriptor(Class<?> clzz,
                                                                                                                                                  Method method) {

        String clzzName = clzz.getName();
        return createMethodDescriptor(clzzName, method);
    }

    public static io.grpc.MethodDescriptor<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> createMethodDescriptor(String clzzName,
                                                                                                                                                  Method method) {
        String methodName = method.getName();
        Class<?> requestType = ReflectUtil.getTypedReq(method);
        Class<?> responseType = ReflectUtil.getTypeRep(method);
        com.google.protobuf.GeneratedMessageV3 argsReq = convertPoJoToPbModel(requestType);
        com.google.protobuf.GeneratedMessageV3 argsRep = convertPoJoToPbModel(responseType);
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

    private static GeneratedMessageV3 convertPoJoToPbModel(Class<?> type) {
        if (!GeneratedMessageV3.class.isAssignableFrom(type)) {
            ProtobufEntity entity = (ProtobufEntity) ReflectUtil.findAnnotation(type, ProtobufEntity.class);
            Class<?> messageType = entity.value();
            if (GeneratedMessageV3.class.isAssignableFrom(type)) {
                Object obj = ReflectUtil.classInstance(messageType);
                return (GeneratedMessageV3) obj;
            }
            return null;
        } else {
            return (GeneratedMessageV3) ReflectUtil.classInstance(type);
        }
    }

    public static String generateFullMethodName(String protocolName, String methodName) {
        return io.grpc.MethodDescriptor.generateFullMethodName(protocolName, methodName);
    }

    public static String generateServiceName(Class<?> protocol) {
        return protocol.getName();
    }
}
