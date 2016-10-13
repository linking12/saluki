package com.quancheng.boot.starter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.boot.starter.service.GreeterService;

@SpringBootApplication
public class DemoServerApp implements CommandLineRunner {

    @SalukiReference(service = "com.quancheng.boot.starter.service.GreeterService", group = "default", version = "1.0.0")
    private GreeterService greeterService;

    public static void main(String[] args) {

        SpringApplication.run(DemoServerApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(greeterService);
    }

}
