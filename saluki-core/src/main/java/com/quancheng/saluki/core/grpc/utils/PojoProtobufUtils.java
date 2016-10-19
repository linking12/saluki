package com.quancheng.saluki.core.grpc.utils;

import org.apache.commons.lang3.StringUtils;

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
            String json = StringUtils.replace(new Gson().toJson(arg), "_", "");
            return gson.fromJson(json, returnType);
        } else {
            return arg;
        }
    }

}
