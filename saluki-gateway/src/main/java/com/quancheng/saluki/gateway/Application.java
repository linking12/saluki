package com.quancheng.saluki.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

import com.quancheng.saluki.gateway.storage.EnableZuulProxyStore;

@EnableAuthorizationServer
@EnableZuulProxy
@EnableZuulProxyStore
@SpringCloudApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
