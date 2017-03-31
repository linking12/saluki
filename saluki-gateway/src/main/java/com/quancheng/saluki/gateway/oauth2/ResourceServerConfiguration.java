/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.oauth2;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

/**
 * @author shimingliu 2017年3月31日 下午2:57:03
 * @version CustomResourceServerConfigurerAdapter.java, v 0.0.1 2017年3月31日 下午2:57:03 shimingliu
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "REST_API";

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID).stateless(false);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        /**
         * @Waing 这里不要随便改动，会导致api调用失败
         */
        http.anonymous()//
            .disable()//
            .requestMatchers()//
            .antMatchers("/api/**")//
            .and().authorizeRequests()//
            .antMatchers("/api/**")//
            .fullyAuthenticated()//
            .and().exceptionHandling()//
            .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }

}
