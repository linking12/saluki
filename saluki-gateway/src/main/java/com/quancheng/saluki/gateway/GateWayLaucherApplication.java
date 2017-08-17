package com.quancheng.saluki.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringCloudApplication
@EnableZuulProxy
@EnableJpaAuditing
public class GateWayLaucherApplication {

  public static void main(String[] args) {
    SpringApplication.run(GateWayLaucherApplication.class, args);
  }

}
