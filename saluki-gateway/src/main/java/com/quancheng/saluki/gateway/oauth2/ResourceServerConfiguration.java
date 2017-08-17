/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

import com.quancheng.saluki.gateway.oauth2.security.CustomAuthenticationEntryPoint;

/**
 * @author shimingliu 2017年3月31日 下午2:57:03
 * @version CustomResourceServerConfigurerAdapter.java, v 0.0.1 2017年3月31日 下午2:57:03 shimingliu
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

  @Autowired
  private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;


  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.anonymous()//
        .disable()//
        .requestMatchers()//
        .antMatchers("/api/**")//
        .and()//
        .authorizeRequests()//
        .antMatchers("/api/**")//
        .fullyAuthenticated()//
        .and()//
        .exceptionHandling()//
        .authenticationEntryPoint(customAuthenticationEntryPoint)
        .accessDeniedHandler(new OAuth2AccessDeniedHandler());

  }

}
