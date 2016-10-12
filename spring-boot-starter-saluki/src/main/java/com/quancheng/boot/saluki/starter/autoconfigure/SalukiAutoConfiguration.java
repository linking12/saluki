package com.quancheng.boot.saluki.starter.autoconfigure;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.quancheng.boot.saluki.starter.SalukiService;
import com.quancheng.boot.saluki.starter.runner.SalukiReferenceRunner;
import com.quancheng.boot.saluki.starter.runner.SalukiServerRunner;

@Configuration
@ConditionalOnProperty(prefix = "grpc", name = "consulIp")
@EnableConfigurationProperties(SalukiProperties.class)
public class SalukiAutoConfiguration {

    private final SalukiProperties grpcProperty;

    public SalukiAutoConfiguration(SalukiProperties grpcProperty){
        this.grpcProperty = grpcProperty;
    }

    @Bean
    @ConditionalOnBean(value = SalukiProperties.class, annotation = SalukiService.class)
    public SalukiServerRunner grpcServerRunner() {
        return new SalukiServerRunner();
    }

    @Bean
    public BeanPostProcessor grpcReferenceRunner() {
        return new SalukiReferenceRunner(grpcProperty);
    }

}
