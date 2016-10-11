package com.quancheng.saluki.core.grpc;

import java.lang.reflect.Method;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.utils.ReflectUtil;
import com.quancheng.saluki.serializer.ProtobufEntity;
import com.quancheng.saluki.serializer.ProtobufSerializer;
import com.quancheng.saluki.serializer.exception.ProtobufException;

public class MethodDescriptorUtils {

    private final static ProtobufSerializer SERIALIZER = new ProtobufSerializer();

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
        com.google.protobuf.GeneratedMessageV3 argsReq = buildDefautInstance(requestType);
        com.google.protobuf.GeneratedMessageV3 argsRep = buildDefautInstance(responseType);
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

    public static GeneratedMessageV3 buildDefautInstance(Class<?> type) {
        if (!GeneratedMessageV3.class.isAssignableFrom(type)) {
            ProtobufEntity entity = (ProtobufEntity) ReflectUtil.findAnnotation(type, ProtobufEntity.class);
            Class<?> messageType = entity.value();
            if (GeneratedMessageV3.class.isAssignableFrom(messageType)) {
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

    public static Object convertPbModelToPojo(GeneratedMessageV3 arg, Class<?> returnType) {
        // 如果期望的结果非pb模型，转一下返回出去，这里对于泛化调用的话存在一些问题
        if (!GeneratedMessageV3.class.isAssignableFrom(returnType)) {
            try {
                return SERIALIZER.fromProtobuf(arg, returnType);
            } catch (ProtobufException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        } else {
            return arg;
        }
    }

    public static Object convertPojoToPbModel(Object arg) {
        if (!(arg instanceof GeneratedMessageV3)) {
            try {
                GeneratedMessageV3 message = (GeneratedMessageV3) SERIALIZER.toProtobuf(arg);
                return message;
            } catch (ProtobufException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        } else {
            return arg;
        }
    }

    public static String covertPojoTypeToPbModelType(String pojoType) {
        try {
            Class<?> parameterType = ReflectUtil.name2class(pojoType);
            ProtobufEntity entity = (ProtobufEntity) ReflectUtil.findAnnotation(parameterType, ProtobufEntity.class);
            Class<?> messageType = entity.value();
            if (GeneratedMessageV3.class.isAssignableFrom(messageType)) {
                return messageType.getName();
            } else {
                return pojoType;
            }
        } catch (ClassNotFoundException e) {
            return pojoType;
        }

    }

}
