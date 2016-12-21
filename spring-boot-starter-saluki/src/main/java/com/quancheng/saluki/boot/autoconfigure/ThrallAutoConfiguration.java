/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.boot.autoconfigure;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.quancheng.saluki.boot.SalukiService;
import com.quancheng.saluki.boot.runner.ThrallReferenceRunner;
import com.quancheng.saluki.boot.runner.ThrallServiceRunner;

/**
 * @author shimingliu 2016年12月16日 下午2:12:42
 * @version ThrallAutoConfiguration.java, v 0.0.1 2016年12月16日 下午2:12:42 shimingliu
 */
@Configuration
@ConditionalOnProperty(prefix = "thrall.grpc", name = "registryAddress")
@AutoConfigureAfter(WebAppAutoConfiguration.class)
@EnableConfigurationProperties(ThrallProperties.class)
public class ThrallAutoConfiguration {

    private final ThrallProperties thrallProperties;

    public ThrallAutoConfiguration(ThrallProperties thrallProperties){
        this.thrallProperties = thrallProperties;
    }

    @Bean
    @ConditionalOnBean(value = ThrallProperties.class, annotation = SalukiService.class)
    public ThrallServiceRunner thrallServiceRunner() {
        return new ThrallServiceRunner(thrallProperties);
    }

    @Bean
    public BeanPostProcessor thrallReferenceRunner() {
        return new ThrallReferenceRunner(thrallProperties);
    }

}
