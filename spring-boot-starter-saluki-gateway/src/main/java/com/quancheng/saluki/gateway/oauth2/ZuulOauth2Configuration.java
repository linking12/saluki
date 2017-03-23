/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.oauth2;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import com.quancheng.saluki.gateway.filters.pre.Oauth2AccessFilter;
import com.quancheng.saluki.gateway.oauth2.limiter.RateLimiter;
import com.quancheng.saluki.gateway.oauth2.support.Oauth2UserStore;

/**
 * @author shimingliu 2017年3月23日 下午12:02:42
 * @version ZuulOAuth2Configuration.java, v 0.0.1 2017年3月23日 下午12:02:42 shimingliu
 */
@Configuration
public class ZuulOauth2Configuration {

    @Autowired
    private DataSource      dataSource;

    @Autowired
    private Oauth2UserStore oauth2UserStore;

    @Autowired
    private RateLimiter     rateLimiter;

    @Bean
    public JdbcTemplate jdbcTemplate() throws Exception {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    @Bean
    public ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }

    @Bean
    public Oauth2AccessFilter oauth2AccessFilter() {
        return new Oauth2AccessFilter(oauth2UserStore, rateLimiter);
    }

}
