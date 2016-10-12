package com.quancheng.boot.starter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.quancheng.boot.starter.saluki.GRpcReference;
import com.quancheng.saluki.core.grpc.service.GenericService;

@SpringBootApplication
public class DemoClientApp implements CommandLineRunner {

    @GRpcReference(interfaceName = "com.quancheng.boot.starter.service.GreeterService", group = "default", version = "1.0.0")
    private GenericService greetService;

    @Override
    public void run(String... args) throws Exception {
        String name = "John";
        com.quancheng.boot.starter.service.model.HelloRequest request = new com.quancheng.boot.starter.service.model.HelloRequest();
        request.setName(name);
        String[] parameterTypes = new String[] { "com.quancheng.boot.starter.service.model.HelloRequest",
                                                 "com.quancheng.boot.starter.service.model.HelloReply" };
        Object[] param = new Object[] { request };
        Object obj = greetService.$invoke("com.quancheng.boot.starter.service.TestGenericService", "SayHello",
                                          parameterTypes, param);
        // final com.quancheng.boot.starter.service.model.HelloReply reply = greetService.SayHello(request);
        System.out.println(obj);
    }

    public static void main(String[] args) {

        SpringApplication.run(DemoClientApp.class, args);
    }

}
