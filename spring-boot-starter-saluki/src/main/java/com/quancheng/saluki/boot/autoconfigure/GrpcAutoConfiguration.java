/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.boot.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.quancheng.saluki.boot.SalukiService;
import com.quancheng.saluki.boot.runner.GrpcReferenceRunner;
import com.quancheng.saluki.boot.runner.GrpcServiceRunner;

/**
 * @author shimingliu 2016年12月16日 下午2:12:42
 * @version ThrallAutoConfiguration.java, v 0.0.1 2016年12月16日 下午2:12:42 shimingliu
 */
@Configuration
@ConditionalOnProperty(prefix = "saluki.grpc", name = "registryAddress")
@AutoConfigureAfter(WebAppAutoConfiguration.class)
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcAutoConfiguration {

    @Autowired
    private GrpcProperties thrallProperties;

    @Bean
    @ConditionalOnBean(value = GrpcProperties.class, annotation = SalukiService.class)
    public GrpcServiceRunner thrallServiceRunner() {
        return new GrpcServiceRunner(thrallProperties);
    }

    @Bean
    public BeanPostProcessor thrallReferenceRunner() {
        return new GrpcReferenceRunner(thrallProperties);
    }

}
