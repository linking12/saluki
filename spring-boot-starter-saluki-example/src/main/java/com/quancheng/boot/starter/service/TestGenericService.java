package com.quancheng.boot.starter.service;

import com.quancheng.boot.starter.saluki.GRpcService;

@GRpcService(interfaceName = "com.quancheng.boot.starter.service.MyGreeterService", group = "default", version = "1.0.0")
public class TestGenericService {

    public com.quancheng.boot.starter.service.model.HelloReply SayHello(com.quancheng.boot.starter.service.model.HelloRequest request) {
        com.quancheng.boot.starter.service.model.HelloReply reply = new com.quancheng.boot.starter.service.model.HelloReply();
        reply.setMessage("hello" + request.getName());
        return reply;
    }
}
