package com.quancheng.boot.starter.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;

import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.saluki.core.common.RpcContext;

import io.undertow.UndertowOptions;

@SpringBootApplication
public class DemoServerApp implements CommandLineRunner, EmbeddedServletContainerCustomizer {

    @Bean
    UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
        UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();
        factory.addBuilderCustomizers(builder -> builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true));
        return factory;
    }

    @SalukiReference(service = "com.quancheng.boot.starter.server.GreeterService", group = "default", version = "1.0.0")
    private GreeterService greeterService;

    public static void main(String[] args) {

        SpringApplication.run(DemoServerApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        com.quancheng.boot.starter.model.HelloRequest request = new com.quancheng.boot.starter.model.HelloRequest();
        request.setName("1234");
        RpcContext.getContext().set("123", "helloworld");
        com.quancheng.boot.starter.model.HelloReply reply = greeterService.SayHello(request);
        System.out.println(reply.getMessage());
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        container.setPort(8181);
    }

}
