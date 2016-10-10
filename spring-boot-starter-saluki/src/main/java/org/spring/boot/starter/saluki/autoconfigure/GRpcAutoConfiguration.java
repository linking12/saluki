package org.spring.boot.starter.saluki.autoconfigure;

import org.spring.boot.starter.saluki.GRpcService;
import org.spring.boot.starter.saluki.runner.GRpcReferenceRunner;
import org.spring.boot.starter.saluki.runner.GRpcServerRunner;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "grpc", name = "consulIp")
@EnableConfigurationProperties(GRpcProperties.class)
public class GRpcAutoConfiguration {

    private final GRpcProperties grpcProperty;

    public GRpcAutoConfiguration(GRpcProperties grpcProperty){
        this.grpcProperty = grpcProperty;
    }

    @Bean
    @ConditionalOnBean(value = GRpcProperties.class, annotation = GRpcService.class)
    public GRpcServerRunner grpcServerRunner() {
        return new GRpcServerRunner();
    }

    @Bean
    public BeanPostProcessor grpcReferenceRunner() {
        return new GRpcReferenceRunner(grpcProperty);
    }

}
