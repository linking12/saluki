/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.boot.autoconfigure;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

/**
 * @author shimingliu 2016年12月20日 下午3:07:34
 * @version MonitorConfiguration.java, v 0.0.1 2016年12月20日 下午3:07:34 shimingliu
 */

public class CommonConfiguration {

    @Configuration
    public static class MyBatisConfig {

        @Bean
        @ConditionalOnMissingBean(EmbeddedDatabase.class)
        public DataSource dataSource() {
            EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
            EmbeddedDatabase db = builder.setType(EmbeddedDatabaseType.H2) //
                                         .setName("grpcmonitor")//
                                         .addScript("mapper/create-db.sql")//
                                         .build();
            return db;
        }

        @Bean(name = "sqlSessionFactory")
        public SqlSessionFactory sqlSessionFactoryBean(DataSource dataSource) {
            SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
            bean.setDataSource(dataSource);
            bean.setTypeAliasesPackage("com.quancheng.saluki.boot.domain");
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            try {
                bean.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
                return bean.getObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Bean
        public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    @Configuration
    @EnableTransactionManagement
    public static class TransactionConfig implements TransactionManagementConfigurer {

        private DataSource dataSource;

        public TransactionConfig(DataSource dataSource){
            this.dataSource = dataSource;
        }

        @Bean
        @Override
        public PlatformTransactionManager annotationDrivenTransactionManager() {
            return new DataSourceTransactionManager(dataSource);
        }

    }

    @Configuration
    @AutoConfigureAfter(MyBatisConfig.class)
    public static class MyBatisMapperScannerConfig {

        @Bean
        public MapperScannerConfigurer mapperScannerConfigurer() {
            MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
            mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
            mapperScannerConfigurer.setBasePackage("com.quancheng.saluki.boot.respository");
            return mapperScannerConfigurer;
        }
    }

}
