/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.alibaba.druid.pool.DruidDataSource;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author shimingliu 2017年3月23日 下午2:24:45
 * @version DataSourceConfiguration.java, v 0.0.1 2017年3月23日 下午2:24:45 shimingliu
 */
@Configuration
public class DataSourceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfiguration.class);

    /**
     * database
     */
    @Value("${spring.datasource.url}")
    private String              dbUrl;

    @Value("${spring.datasource.username}")
    private String              username;

    @Value("${spring.datasource.password}")
    private String              password;

    @Value("${spring.datasource.driverClassName}")
    private String              driverClassName;

    @Value("${spring.datasource.initialSize}")
    private int                 initialSize;

    @Value("${spring.datasource.minIdle}")
    private int                 minIdle;

    @Value("${spring.datasource.maxActive}")
    private int                 maxActive;

    @Value("${spring.datasource.maxWait}")
    private int                 maxWait;

    @Value("${spring.datasource.timeBetweenEvictionRunsMillis}")
    private int                 timeBetweenEvictionRunsMillis;

    @Value("${spring.datasource.minEvictableIdleTimeMillis}")
    private int                 minEvictableIdleTimeMillis;

    @Value("${spring.datasource.validationQuery}")
    private String              validationQuery;

    @Value("${spring.datasource.testWhileIdle}")
    private boolean             testWhileIdle;

    @Value("${spring.datasource.testOnBorrow}")
    private boolean             testOnBorrow;

    @Value("${spring.datasource.testOnReturn}")
    private boolean             testOnReturn;

    @Value("${spring.datasource.poolPreparedStatements}")
    private boolean             poolPreparedStatements;

    @Value("${spring.datasource.maxPoolPreparedStatementPerConnectionSize}")
    private int                 maxPoolPreparedStatementPerConnectionSize;

    @Value("${spring.datasource.filters}")
    private String              filters;

    @Value("{spring.datasource.connectionProperties}")
    private String              connectionProperties;

    /**
     * redis
     */
    @Value("${spring.redis.pool.max-wait}")
    private long                maxWaitMillis;
    @Value("${spring.redis.pool.max-idle}")
    private int                 maxIdle;
    @Value("${spring.redis.host}")
    private String              host;
    @Value("${spring.redis.port}")
    private int                 port;
    @Value("${spring.redis.password}")
    private String              redisPassord;

    @Bean
    @Primary
    public DataSource dataSource() {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(this.dbUrl);
        datasource.setUsername(username);
        datasource.setPassword(password);
        datasource.setDriverClassName(driverClassName);
        // configuration
        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxActive(maxActive);
        datasource.setMaxWait(maxWait);
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setValidationQuery(validationQuery);
        datasource.setTestWhileIdle(testWhileIdle);
        datasource.setTestOnBorrow(testOnBorrow);
        datasource.setTestOnReturn(testOnReturn);
        datasource.setPoolPreparedStatements(poolPreparedStatements);
        datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        try {
            datasource.setFilters(filters);
        } catch (SQLException e) {
            logger.error("druid configuration initialization filter", e);
        }
        datasource.setConnectionProperties(connectionProperties);

        return datasource;
    }

    @Bean
    public JedisPool redisPoolFactory() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port, 0, redisPassord);
        return jedisPool;
    }
}
