package com.quancheng.saluki.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({ "com.quancheng.saluki.monitor.web", "com.quancheng.saluki.example.server" })
@SpringBootApplication
public class SalukiExampleServerApp implements EmbeddedServletContainerCustomizer {

    public static void main(String[] args) {

        SpringApplication.run(SalukiExampleServerApp.class, args);
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        container.setPort(8080);
    }

}
