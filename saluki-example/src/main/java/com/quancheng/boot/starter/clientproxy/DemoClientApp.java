package com.quancheng.boot.starter.clientproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.quancheng.boot.starter.server.GreeterService;
import com.quancheng.saluki.core.common.RpcContext;

@SpringBootApplication
public class DemoClientApp {

    public static void main(String[] args) {

        SpringApplication.run(DemoClientApp.class, args);
    }

    // public static class Task implements Runnable {
    //
    // private GreeterService greeterService;
    //
    // public Task(GreeterService greeterService){
    // this.greeterService = greeterService;
    // }
    //
    // public void run() {
    // long starTime = System.currentTimeMillis();
    // com.quancheng.boot.starter.model.HelloRequest request = new com.quancheng.boot.starter.model.HelloRequest();
    // request.setName("joe");
    // RpcContext.getContext().set("123", "helloworld");
    // com.quancheng.boot.starter.model.HelloReply reply = greeterService.SayHello(request);
    // long endTime = System.currentTimeMillis();
    // System.out.println("耗时：" + (endTime - starTime) + "毫秒");
    // System.out.println(reply.getMessage());
    // }
    // }

}
