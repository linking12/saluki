package com.quancheng.saluki.core.grpc.utils;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.quancheng.saluki.serializer.IProtobufSerializer;
import com.quancheng.saluki.serializer.ProtobufSerializer;
import com.quancheng.saluki.serializer.exception.ProtobufException;

public class PojoProtobufUtils {

    private final static IProtobufSerializer serializer;
    private final static Gson                gson;

    static {
        gson = new Gson();
        serializer = new ProtobufSerializer();
    }

    private PojoProtobufUtils(){
    }

    public static Message Pojo2Protobuf(Object arg) throws ProtobufException {
        if (!(arg instanceof Message)) {
            Message message = (Message) serializer.toProtobuf(arg);
            arg = null;
            return message;
        }
        return (Message) arg;
    }

    public static Object Protobuf2Pojo(Message arg, Class<? extends Object> returnType) throws ProtobufException {
        if (!Message.class.isAssignableFrom(returnType)) {
            return serializer.fromProtobuf(arg, returnType);
        } else {
            return arg;
        }
    }

}
