package com.quancheng.saluki.core.grpc.utils;

import java.lang.reflect.Method;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.utils.ReflectUtil;
import com.quancheng.saluki.serializer.ProtobufEntity;

public class MethodDescriptorUtils {

    public static io.grpc.MethodDescriptor<Message, Message> createMethodDescriptor(Class<?> clzz, Method method) {

        String clzzName = clzz.getName();
        return createMethodDescriptor(clzzName, method);
    }

    public static io.grpc.MethodDescriptor<Message, Message> createMethodDescriptor(String clzzName, Method method) {
        String methodName = method.getName();
        Class<?> requestType = ReflectUtil.getTypedReq(method);
        Class<?> responseType = ReflectUtil.getTypeRep(method);
        Message argsReq = buildDefaultInstance(requestType);
        Message argsRep = buildDefaultInstance(responseType);
        return createMethodDescriptor(clzzName, methodName, argsReq, argsRep);
    }

    public static io.grpc.MethodDescriptor<Message, Message> createMethodDescriptor(String clzzName, String methodName,
                                                                                    Message argsReq, Message argsRep) {
        String fullMethodName = io.grpc.MethodDescriptor.generateFullMethodName(clzzName, methodName);
        return io.grpc.MethodDescriptor.create(io.grpc.MethodDescriptor.MethodType.UNARY, fullMethodName,
                                               io.grpc.protobuf.ProtoUtils.marshaller(argsReq),
                                               io.grpc.protobuf.ProtoUtils.marshaller(argsRep));
    }

    public static Message buildDefaultInstance(Class<?> type) {
        Class<? extends Message> messageType;
        if (!Message.class.isAssignableFrom(type)) {
            ProtobufEntity entity = (ProtobufEntity) ReflectUtil.findAnnotation(type, ProtobufEntity.class);
            messageType = entity.value();
        } else {
            messageType = (Class<? extends Message>) type;
        }
        Object obj = ReflectUtil.classInstance(messageType);
        return (Message) obj;
    }

}
