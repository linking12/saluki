package com.quancheng.saluki.example.server;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.boot.SalukiService;

@SalukiService
public class HelloServiceImpl implements HelloService {

    @Override
    public HelloReply sayHello(HelloRequest request) {
        HelloReply reply = new HelloReply();
        int registryPort = 0;
        // Preconditions.checkState(registryPort != 0, "RegistryPort can not be null", registryPort);
        reply.setMessage(request.getName());
        return reply;
    }

}
