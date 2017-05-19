/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.util;

import java.lang.reflect.Method;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.utils.ReflectUtils;
import com.quancheng.saluki.serializer.ProtobufEntity;

/**
 * @author shimingliu 2016年12月14日 下午9:29:43
 * @version MethodDescriptorUtil.java, v 0.0.1 2016年12月14日 下午9:29:43 shimingliu
 */
public final class MethodDescriptorUtil {

    public static io.grpc.MethodDescriptor<Message, Message> createMethodDescriptor(Class<?> clzz, Method method) {
        String clzzName = clzz.getName();
        String methodName = method.getName();
        Class<?> requestType = ReflectUtils.getTypedOfReq(method);
        Class<?> responseType = ReflectUtils.getTypeOfRep(method);
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

    @SuppressWarnings("unchecked")
    public static Message buildDefaultInstance(Class<?> type) {
        Class<? extends Message> messageType;
        if (!Message.class.isAssignableFrom(type)) {
            ProtobufEntity entity = (ProtobufEntity) ReflectUtils.findAnnotationFromClass(type, ProtobufEntity.class);
            messageType = entity.value();
        } else {
            messageType = (Class<? extends Message>) type;
        }
        Object obj = ReflectUtils.classInstance(messageType);
        return (Message) obj;
    }
}
