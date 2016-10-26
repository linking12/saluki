package com.quancheng.boot.starter.clientstub;

import org.lognet.springboot.grpc.proto.GreeterGrpc;
import org.lognet.springboot.grpc.proto.GreeterOuterClass;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.examples.service.HelloServiceGrpc;

@SpringBootApplication
public class DemoClientApp implements CommandLineRunner {

    @SalukiReference(service = "com.quancheng.boot.starter.server.GreeterService", group = "Default", version = "1.0.0")
    private GreeterGrpc.GreeterFutureStub           greeterFutureStub;

    @SalukiReference(service = "com.quancheng.examples.service.HelloService", group = "Default", version = "1.0.0")
    private HelloServiceGrpc.HelloServiceFutureStub helloserviceFutureStub;

    @Override
    public void run(String... args) throws Exception {
        String name = "John";
        // final GreeterOuterClass.HelloRequest helloRequest =
        // GreeterOuterClass.HelloRequest.newBuilder().setName(name).build();
        //
        // final String reply = greeterFutureStub.sayHello(helloRequest).get().getMessage();
        // System.out.println(reply);

        com.quancheng.examples.model.Hello.HelloRequest request = com.quancheng.examples.model.Hello.HelloRequest.newBuilder().setName(name).build();
        com.quancheng.examples.model.Hello.HelloReply reply = helloserviceFutureStub.sayHello(request).get();
        System.out.println(reply);
    }

    public static void main(String[] args) {

        SpringApplication.run(DemoClientApp.class, args);
    }

}
