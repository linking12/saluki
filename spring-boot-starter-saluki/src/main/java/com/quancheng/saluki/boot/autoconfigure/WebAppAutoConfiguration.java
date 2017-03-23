/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

/**
 * @author shimingliu 2016年12月17日 上午12:14:31
 * @version WebAppAutoConfiguration.java, v 0.0.1 2016年12月17日 上午12:14:31 shimingliu
 */

@Configuration
@ConditionalOnBean(value = GrpcProperties.class)
public class WebAppAutoConfiguration implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    private final GrpcProperties thrallProperties;

    public WebAppAutoConfiguration(GrpcProperties thrallProperties){
        this.thrallProperties = thrallProperties;
    }

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        int httpPort = event.getEmbeddedServletContainer().getPort();
        if (thrallProperties.getRegistryHttpPort() == 0) {
            if (httpPort != 0) {
                thrallProperties.setRegistryHttpPort(httpPort);
            }
        }
    }

}
