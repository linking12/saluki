package com.quancheng.saluki.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.core.common.RpcContext;

@SpringBootApplication
public class SalukiExampleClientApp implements CommandLineRunner {

    @SalukiReference
    private HelloService helloService;

    public static void main(String[] args) {

        SpringApplication.run(SalukiExampleClientApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        HelloRequest request = new HelloRequest();
        request.setName("liushiming");
        RpcContext.getContext().set("123", "helloworld");
        HelloReply reply = helloService.sayHello(request);
        System.out.print(reply);

    }

}
