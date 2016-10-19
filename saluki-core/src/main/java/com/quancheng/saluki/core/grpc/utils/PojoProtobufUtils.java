package com.quancheng.saluki.core.grpc.utils;

import com.google.protobuf.Message;
import com.quancheng.saluki.serializer.IProtobufSerializer;
import com.quancheng.saluki.serializer.ProtobufSerializer;
import com.quancheng.saluki.serializer.exception.ProtobufException;

public class PojoProtobufUtils {

    private final static IProtobufSerializer serializer;

    static {
        serializer = new ProtobufSerializer();
    }

    private PojoProtobufUtils(){
    }

    public static Message Pojo2Protobuf(Object arg) {
        if (!(arg instanceof Message)) {
            try {
                Message message = (Message) serializer.toProtobuf(arg);
                arg = null;
                return message;
            } catch (ProtobufException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
        return (Message) arg;
    }

    public static Object Protobuf2Pojo(Message arg, Class<? extends Object> returnType) {
        if (!Message.class.isAssignableFrom(returnType)) {
            try {
                return serializer.fromProtobuf(arg, returnType);
            } catch (ProtobufException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        } else {
            return arg;
        }
    }

}
