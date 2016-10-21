package com.quancheng.boot.starter.server;

import com.quancheng.boot.saluki.starter.SalukiService;
import com.quancheng.saluki.core.common.RpcContext;

@SalukiService(service = "com.quancheng.boot.starter.server.GreeterService")
public class GreeterServiceImpl implements GreeterService {

    @Override
    public com.quancheng.boot.starter.model.HelloReply SayHello(com.quancheng.boot.starter.model.HelloRequest request) {
        com.quancheng.boot.starter.model.HelloReply reply = new com.quancheng.boot.starter.model.HelloReply();
        reply.setMessage("hello" + request.getName());
        System.out.println(RpcContext.getContext().get("123"));
        return reply;
    }

}
