package com.quancheng.saluki.example.client.proxy;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.core.common.RpcContext;

@RestController
@RequestMapping("/proxy")
public class ProxyServiceController {

    @SalukiReference(service = "com.quancheng.examples.service.HelloService", group = "Default", version = "1.0.0")
    private HelloService helloService;

    @RequestMapping("/hello")
    public HelloReply view() {
        HelloRequest request = new HelloRequest();
        request.setName("liushiming");
        RpcContext.getContext().set("123", "helloworld");
        HelloReply reply = helloService.sayHello(request);
        return reply;
    }
}
