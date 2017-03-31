package com.quancheng.saluki.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class GateWayLaucherApplication {

    public static void main(String[] args) {
        SpringApplication.run(GateWayLaucherApplication.class, args);
    }

}
