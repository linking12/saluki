package com.quancheng.boot.starter.clientproxy;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.boot.starter.model.HelloReply;
import com.quancheng.boot.starter.server.GreeterService;
import com.quancheng.saluki.core.common.RpcContext;

@RestController
@RequestMapping("/greeter")
public class GreeterServiceController {

    @SalukiReference(service = "com.quancheng.boot.starter.server.GreeterService", group = "default", version = "1.0.0")
    private GreeterService greeterService;

    @RequestMapping
    public HelloReply view() {

        com.quancheng.boot.starter.model.HelloRequest request = new com.quancheng.boot.starter.model.HelloRequest();
        request.setName("liushiming");
        RpcContext.getContext().set("123", "helloworld");
        com.quancheng.boot.starter.model.HelloReply reply = greeterService.SayHello(request);
        return reply;
    }
}
