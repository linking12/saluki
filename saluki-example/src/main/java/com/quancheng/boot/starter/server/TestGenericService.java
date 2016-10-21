package com.quancheng.boot.starter.server;

import com.quancheng.boot.saluki.starter.SalukiService;

@SalukiService(service = "com.quancheng.boot.starter.server.MyGreeterService", group = "Default", version = "1.0.0")
public class TestGenericService {

    public com.quancheng.boot.starter.model.HelloReply SayHello(com.quancheng.boot.starter.model.HelloRequest request) {
        com.quancheng.boot.starter.model.HelloReply reply = new com.quancheng.boot.starter.model.HelloReply();
        reply.setMessage("hello" + request.getName());
        return reply;
    }
}
