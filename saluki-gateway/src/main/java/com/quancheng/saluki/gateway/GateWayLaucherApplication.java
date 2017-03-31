package com.quancheng.saluki.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@EnableZuulProxy
@EnableResourceServer
@EnableAuthorizationServer
@SpringCloudApplication
public class GateWayLaucherApplication {

    public static void main(String[] args) {
        SpringApplication.run(GateWayLaucherApplication.class, args);
    }

}
