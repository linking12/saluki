package com.quancheng.saluki.example.client.proxy;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.test.model.user.UserCreateRequest;
import com.quancheng.test.model.user.UserCreateResponse;

@RestController
@RequestMapping("/proxy")
public class ProxyServiceController {

    @SalukiReference(retries = 3)
    private HelloService                           helloService;

    @SalukiReference
    private com.quancheng.test.service.UserService userService;

    @RequestMapping("/hello")
    public HelloReply hello() {
        return call();
    }

    private HelloReply call() {
        HelloRequest request = new HelloRequest();
        request.setName("liushiming");
        RpcContext.getContext().set("123", "helloworld");
        HelloReply reply = helloService.sayHello(request);
        return reply;
    }

    @RequestMapping("/user")
    public UserCreateResponse user() {
        UserCreateRequest request = new UserCreateRequest();
        UserCreateResponse reply = userService.create(request);
        return reply;
    }
}
