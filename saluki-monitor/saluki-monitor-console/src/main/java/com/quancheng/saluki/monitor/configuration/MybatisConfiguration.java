/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.monitor.configuration;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author shimingliu 2016年12月20日 下午3:07:34
 * @version MonitorConfiguration.java, v 0.0.1 2016年12月20日 下午3:07:34 shimingliu
 */
@Configuration
@ConditionalOnExpression("${saluki.monitor.enabled:true}")
public class MybatisConfiguration {

    private static final Logger         log = LoggerFactory.getLogger(MybatisConfiguration.class);

    @Autowired
    private MybatisDataSourceProperties config;

    /**
     * 创建 druid数据源
     */
    @Bean(initMethod = "init")
    public DataSource druidDataSource() {
        System.setProperty("druid.logType", "log4j2");
        // 不初始化数据源连接
        if (config == null || config.getUrl() == null) {
            return null;
        }
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setName(config.getName());
        druidDataSource.setUrl(config.getUrl());
        druidDataSource.setUsername(config.getUsername());
        druidDataSource.setPassword(config.getPassword());
        druidDataSource.setPoolPreparedStatements(config.isPoolPreparedStatements());
        druidDataSource.setInitialSize(config.getInitialSize());
        druidDataSource.setMinIdle(config.getMinIdle());
        druidDataSource.setMaxActive(config.getMaxActive());
        druidDataSource.setMaxWait(config.getMaxWait());
        druidDataSource.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
        druidDataSource.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
        druidDataSource.setValidationQuery(config.getValidationQuery());
        druidDataSource.setTestWhileIdle(config.isTestWhileIdle());
        druidDataSource.setTestOnBorrow(config.isTestOnBorrow());
        druidDataSource.setTestOnReturn(config.isTestOnReturn());
        druidDataSource.setPoolPreparedStatements(config.isPoolPreparedStatements());
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(config.getMaxPoolPreparedStatementPerConnectionSize());
        // 定期输出统计信息到日志中,先关闭
        // druidDataSource.setTimeBetweenLogStatsMillis();
        druidDataSource.setConnectionProperties(config.getConnectionProperties());
        druidDataSource.setUseGlobalDataSourceStat(true);
        try {
            druidDataSource.setFilters(config.getFilters());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return druidDataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setTypeAliasesPackage("com.quancheng.saluki.domain");
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            bean.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
            return bean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        if (sqlSessionFactory == null) {
            return null;
        }
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
