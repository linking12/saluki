/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.configuration;

import org.springframework.boot.autoconfigure.velocity.VelocityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author shimingliu 2017年2月13日 下午3:38:08
 * @version VelocityConfig.java, v 0.0.1 2017年2月13日 下午3:38:08 shimingliu
 */
@SuppressWarnings("deprecation")
@Configuration
public class VelocityConfiguration {

    @Bean(name = "velocityViewResolver")
    public EmbeddedVelocityLayoutViewResolver velocityViewResolver(VelocityProperties properties) {
        EmbeddedVelocityLayoutViewResolver viewResolver = new EmbeddedVelocityLayoutViewResolver();
        viewResolver.setViewClass(EmbeddedVelocityLayoutView.class);
        properties.applyToViewResolver(viewResolver);
        viewResolver.setLayoutUrl("layout/main.vm");
        return viewResolver;
    }

}
