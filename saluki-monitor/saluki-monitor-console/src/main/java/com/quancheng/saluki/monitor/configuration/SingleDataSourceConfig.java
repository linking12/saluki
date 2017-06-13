/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.quancheng.saluki.monitor.configuration;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.alibaba.druid.pool.DruidDataSource;
import com.quancheng.saluki.monitor.configuration.SingleDataSourceConfig.DataSourcePropertiesCommon;


/**
 * @author liushiming
 * @version DataSourceConfig.java, v 0.0.1 2017年6月7日 下午2:55:32 liushiming
 * @since JDK 1.8
 */
@EnableConfigurationProperties(DataSourcePropertiesCommon.class)
public class SingleDataSourceConfig {

  @Autowired
  protected DataSourcePropertiesCommon commonProperties;

  protected DataSource createDataSource() throws SQLException {
    // special
    DruidDataSource datasource = new DruidDataSource();
    datasource.setUrl(commonProperties.getUrl());
    datasource.setUsername(commonProperties.getUsername());
    datasource.setPassword(commonProperties.getPassword());
    // common
    datasource.setDriverClassName(commonProperties.getDriverClassName());
    datasource.setInitialSize(commonProperties.getInitialSize());
    datasource.setMinIdle(commonProperties.getMinIdle());
    datasource.setMaxActive(commonProperties.getMaxActive());
    datasource.setMaxWait(commonProperties.getMaxWait());
    datasource
        .setTimeBetweenEvictionRunsMillis(commonProperties.getTimeBetweenEvictionRunsMillis());
    datasource.setMinEvictableIdleTimeMillis(commonProperties.getMinEvictableIdleTimeMillis());
    datasource.setValidationQuery(commonProperties.getValidationQuery());
    datasource.setTestWhileIdle(commonProperties.isTestWhileIdle());
    datasource.setTestOnBorrow(commonProperties.isTestOnBorrow());
    datasource.setTestOnReturn(commonProperties.isTestOnReturn());
    datasource.setPoolPreparedStatements(commonProperties.isPoolPreparedStatements());
    datasource.setMaxPoolPreparedStatementPerConnectionSize(
        commonProperties.getMaxPoolPreparedStatementPerConnectionSize());
    datasource.setFilters(commonProperties.getFilters());
    datasource.setConnectionProperties(commonProperties.getConnectionProperties());
    return datasource;
  }

  @ConfigurationProperties(prefix = "spring.datasource")
  protected static class DataSourcePropertiesCommon {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private int initialSize;
    private int minIdle;
    private int maxActive;
    private int maxWait;
    private int timeBetweenEvictionRunsMillis;
    private int minEvictableIdleTimeMillis;
    private String validationQuery;
    private boolean testWhileIdle;
    private boolean testOnBorrow;
    private boolean testOnReturn;
    private boolean poolPreparedStatements;
    private int maxPoolPreparedStatementPerConnectionSize;
    private String filters;
    private String connectionProperties;

    /**
     * @return the driverClassName
     */
    public String getDriverClassName() {
      return driverClassName;
    }

    /**
     * @param driverClassName the driverClassName to set
     */
    public void setDriverClassName(String driverClassName) {
      this.driverClassName = driverClassName;
    }

    /**
     * @return the initialSize
     */
    public int getInitialSize() {
      return initialSize;
    }

    /**
     * @param initialSize the initialSize to set
     */
    public void setInitialSize(int initialSize) {
      this.initialSize = initialSize;
    }

    /**
     * @return the minIdle
     */
    public int getMinIdle() {
      return minIdle;
    }

    /**
     * @param minIdle the minIdle to set
     */
    public void setMinIdle(int minIdle) {
      this.minIdle = minIdle;
    }

    /**
     * @return the maxActive
     */
    public int getMaxActive() {
      return maxActive;
    }

    /**
     * @param maxActive the maxActive to set
     */
    public void setMaxActive(int maxActive) {
      this.maxActive = maxActive;
    }

    /**
     * @return the maxWait
     */
    public int getMaxWait() {
      return maxWait;
    }

    /**
     * @param maxWait the maxWait to set
     */
    public void setMaxWait(int maxWait) {
      this.maxWait = maxWait;
    }

    /**
     * @return the timeBetweenEvictionRunsMillis
     */
    public int getTimeBetweenEvictionRunsMillis() {
      return timeBetweenEvictionRunsMillis;
    }

    /**
     * @param timeBetweenEvictionRunsMillis the timeBetweenEvictionRunsMillis to set
     */
    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
      this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    /**
     * @return the minEvictableIdleTimeMillis
     */
    public int getMinEvictableIdleTimeMillis() {
      return minEvictableIdleTimeMillis;
    }

    /**
     * @param minEvictableIdleTimeMillis the minEvictableIdleTimeMillis to set
     */
    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
      this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    /**
     * @return the validationQuery
     */
    public String getValidationQuery() {
      return validationQuery;
    }

    /**
     * @param validationQuery the validationQuery to set
     */
    public void setValidationQuery(String validationQuery) {
      this.validationQuery = validationQuery;
    }

    /**
     * @return the testWhileIdle
     */
    public boolean isTestWhileIdle() {
      return testWhileIdle;
    }

    /**
     * @param testWhileIdle the testWhileIdle to set
     */
    public void setTestWhileIdle(boolean testWhileIdle) {
      this.testWhileIdle = testWhileIdle;
    }

    /**
     * @return the testOnBorrow
     */
    public boolean isTestOnBorrow() {
      return testOnBorrow;
    }

    /**
     * @param testOnBorrow the testOnBorrow to set
     */
    public void setTestOnBorrow(boolean testOnBorrow) {
      this.testOnBorrow = testOnBorrow;
    }

    /**
     * @return the testOnReturn
     */
    public boolean isTestOnReturn() {
      return testOnReturn;
    }

    /**
     * @param testOnReturn the testOnReturn to set
     */
    public void setTestOnReturn(boolean testOnReturn) {
      this.testOnReturn = testOnReturn;
    }

    /**
     * @return the poolPreparedStatements
     */
    public boolean isPoolPreparedStatements() {
      return poolPreparedStatements;
    }

    /**
     * @param poolPreparedStatements the poolPreparedStatements to set
     */
    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
      this.poolPreparedStatements = poolPreparedStatements;
    }

    /**
     * @return the maxPoolPreparedStatementPerConnectionSize
     */
    public int getMaxPoolPreparedStatementPerConnectionSize() {
      return maxPoolPreparedStatementPerConnectionSize;
    }

    /**
     * @param maxPoolPreparedStatementPerConnectionSize the
     *        maxPoolPreparedStatementPerConnectionSize to set
     */
    public void setMaxPoolPreparedStatementPerConnectionSize(
        int maxPoolPreparedStatementPerConnectionSize) {
      this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }

    /**
     * @return the filters
     */
    public String getFilters() {
      return filters;
    }

    /**
     * @param filters the filters to set
     */
    public void setFilters(String filters) {
      this.filters = filters;
    }

    /**
     * @return the connectionProperties
     */
    public String getConnectionProperties() {
      return connectionProperties;
    }

    /**
     * @param connectionProperties the connectionProperties to set
     */
    public void setConnectionProperties(String connectionProperties) {
      this.connectionProperties = connectionProperties;
    }

    /**
     * @return the url
     */
    public String getUrl() {
      return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
      this.url = url;
    }

    /**
     * @return the username
     */
    public String getUsername() {
      return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
      this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
      return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
      this.password = password;
    }


  }


}
