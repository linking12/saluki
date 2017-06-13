/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.monitor.configuration;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author shimingliu 2016年12月20日 下午3:07:34
 * @version MonitorConfiguration.java, v 0.0.1 2016年12月20日 下午3:07:34 shimingliu
 */
@Configuration
public class MybatisConfiguration extends SingleDataSourceConfig {

  @Bean
  public DataSource datasource(@Value("${spring.datasource.url}") String url,
      @Value("${spring.datasource.username}") String userName,
      @Value("${spring.datasource.password}") String passWord) throws SQLException {
    return createDataSource(url, userName, passWord);
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
