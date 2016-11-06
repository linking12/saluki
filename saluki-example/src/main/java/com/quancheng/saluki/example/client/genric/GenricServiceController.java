package com.quancheng.saluki.example.client.genric;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.saluki.core.grpc.service.GenericService;

@RestController
@RequestMapping("/genric")
public class GenricServiceController {

    @SalukiReference(service = "com.quancheng.examples.service.HelloService", group = "Default", version = "1.0.0")
    private GenericService genricService;

    @RequestMapping("/hello")
    public HelloReply view() {
        String serviceName = "com.quancheng.examples.service.HelloService";
        String method = "sayHello";
        String[] parameterTypes = new String[] { "com.quancheng.examples.model.hello.HelloRequest",
                                                 "com.quancheng.examples.model.hello.HelloReply" };
        HelloRequest request = new HelloRequest();
        request.setName("liushiming");
        Object[] args1 = new Object[] { request };
        HelloReply reply = (HelloReply) genricService.$invoke(serviceName, method, parameterTypes, args1);
        return reply;
    }
}
