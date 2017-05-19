package com.quancheng.saluki.example.client.proxy;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.examples.service.HelloService2;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.core.common.RpcContext;

@RestController
@RequestMapping("/proxy")
public class ProxyServiceController {

  @SalukiReference(retries = 3)
  private HelloService helloService;


  @SalukiReference
  private HelloService2 helloService2;


  @RequestMapping("/hello")
  public HelloReply hello() {
    return call();
  }

  @RequestMapping("/hello2")
  public HelloReply hello2() {
    return call2();
  }

  private HelloReply call() {
    HelloRequest request = new HelloRequest();
    request.setName("liushiming");
    RpcContext.getContext().set("123", "helloworld");
    HelloReply reply = helloService.sayHello(request);
    return reply;
  }

  private HelloReply call2() {
    HelloRequest request = new HelloRequest();
    request.setName("liushiming");
    RpcContext.getContext().set("123", "helloworld");
    HelloReply reply = helloService2.sayHello(request);
    return reply;
  }

}
