package com.quancheng.saluki.example.server;

import com.google.common.base.Preconditions;
import com.quancheng.boot.saluki.starter.SalukiService;
import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;

@SalukiService(service = "com.quancheng.examples.service.HelloService", group = "Default", version = "1.0.0")
public class HelloServiceImpl implements HelloService {

    @Override
    public HelloReply sayHello(HelloRequest request) {
        HelloReply reply = new HelloReply();
        reply.setMessage(request.getName());
        int registryPort = 0;
        //Preconditions.checkState(registryPort != 0, "RegistryPort can not be null", registryPort);
        return reply;
    }

}
