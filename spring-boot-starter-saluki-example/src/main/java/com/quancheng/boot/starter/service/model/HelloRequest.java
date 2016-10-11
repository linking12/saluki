package com.quancheng.boot.starter.service.model;

import com.quancheng.saluki.serializer.ProtobufAttribute;
import com.quancheng.saluki.serializer.ProtobufEntity;

@ProtobufEntity(org.lognet.springboot.grpc.proto.GreeterOuterClass.HelloRequest.class)
public class HelloRequest {

    @ProtobufAttribute
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
