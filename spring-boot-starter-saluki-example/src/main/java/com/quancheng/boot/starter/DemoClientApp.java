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
        final GreeterOuterClass.HelloRequest helloRequest = GreeterOuterClass.HelloRequest.newBuilder().setName(name).build();
        
        com.quancheng.boot.starter.service.HelloRequest request = new   com.quancheng.boot.starter.service.HelloRequest();
        request.setName("asdf");
        

        final GreeterOuterClass.HelloReply reply = greetService.SayHello(helloRequest);
        System.out.println(reply);
    }

    public static void main(String[] args) {

        SpringApplication.run(DemoClientApp.class, args);
    }

}
