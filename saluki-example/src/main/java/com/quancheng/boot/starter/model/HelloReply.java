package com.quancheng.boot.starter.model;

import com.quancheng.saluki.serializer.ProtobufAttribute;
import com.quancheng.saluki.serializer.ProtobufEntity;

@ProtobufEntity(org.lognet.springboot.grpc.proto.GreeterOuterClass.HelloReply.class)
public class HelloReply {

    @ProtobufAttribute
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
