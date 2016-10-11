package com.quancheng.boot.starter;

import org.lognet.springboot.grpc.proto.GreeterOuterClass;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.quancheng.boot.starter.saluki.GRpcReference;
import com.quancheng.boot.starter.service.GreeterService;

@SpringBootApplication
public class DemoClientApp implements CommandLineRunner {

    @GRpcReference(interfaceName = "com.quancheng.boot.starter.service.GreeterService", group = "default", version = "1.0.0")
    private GreeterService greetService;

    @Override
    public void run(String... args) throws Exception {
        String name = "John";
        com.quancheng.boot.starter.service.model.HelloRequest request = new com.quancheng.boot.starter.service.model.HelloRequest();
        request.setName(name);
        final com.quancheng.boot.starter.service.model.HelloReply reply = greetService.SayHello(request);
        System.out.println(reply);
    }

    public static void main(String[] args) {

        SpringApplication.run(DemoClientApp.class, args);
    }

}
