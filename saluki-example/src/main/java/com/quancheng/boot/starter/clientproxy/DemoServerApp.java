package com.quancheng.boot.starter.clientproxy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.boot.starter.server.GreeterService;
import com.quancheng.saluki.core.common.RpcContext;

@SpringBootApplication
public class DemoServerApp implements CommandLineRunner {

    @SalukiReference(service = "com.quancheng.boot.starter.server.GreeterService", group = "default", version = "1.0.0")
    private GreeterService greeterService;

    public static void main(String[] args) {

        SpringApplication.run(DemoServerApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(new Task(greeterService));
            thread.start();
        }
    }

    public static class Task implements Runnable {

        private GreeterService greeterService;

        public Task(GreeterService greeterService){
            this.greeterService = greeterService;
        }

        public void run() {
            long starTime = System.currentTimeMillis();
            com.quancheng.boot.starter.model.HelloRequest request = new com.quancheng.boot.starter.model.HelloRequest();
            request.setName("joe");
            RpcContext.getContext().set("123", "helloworld");
            com.quancheng.boot.starter.model.HelloReply reply = greeterService.SayHello(request);
            long endTime = System.currentTimeMillis();
            System.out.println("耗时：" + (endTime - starTime) + "毫秒");
            System.out.println(reply.getMessage());
        }
    }

}
