/*
 * Copyright 1999-2012 DianRong.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.saluki.monitor.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <strong>描述：</strong>TODO 描述 <br>
 * <strong>功能：</strong><br>
 * <strong>使用场景：</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author liushiming 2017年4月26日 上午10:11:28
 * @version $Id: MybatisDataSourceProperties.java, v 0.0.1 2017年4月26日 上午10:11:28 liushiming Exp $
 */
@ConfigurationProperties(prefix = "datasource")
public class MybatisDataSourceProperties {

    // 配置这个属性的意义在于，如果存在多个数据源，监控的时候可以通过名字来区分开来。
    private String  name;
    // 连接数据库的url，不同数据库不一样
    private String  url;
    // 连接数据库的用户名
    private String  username;
    // 连接数据库的密码
    private String  password;
    // 初始化时建立物理连接的个数。初始化发生在显示调用init方法,或者第一次getConnection时
    private int     initialSize                               = 5;
    // 最小连接池数量
    private int     minIdle                                   = 5;
    // 最大连接池数量
    private int     maxActive                                 = 20;
    // 获取连接时最大等待时间，单位毫秒
    private int     maxWait                                   = 60000;
    /**
     * 有两个含义：1) Destroy线程会检测连接的间隔时间，如果连接空闲时间大于等于minEvictableIdleTimeMillis则关闭物理连接
     * 2)testWhileIdle的判断依据，详细看testWhileIdle属性的说明
     */
    private int     timeBetweenEvictionRunsMillis             = 60000;
    // 连接保持空闲而不被驱逐的最长时间
    private int     minEvictableIdleTimeMillis                = 300000;
    /**
     * 用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。
     */
    private String  validationQuery                           = "SELECT 'x'";
    // 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
    private boolean testWhileIdle                             = true;
    // 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
    private boolean testOnBorrow                              = false;
    // 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
    private boolean testOnReturn                              = false;
    // 是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭。
    private boolean poolPreparedStatements                    = false;
    // 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。
    // 在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
    private int     maxPoolPreparedStatementPerConnectionSize = -1;
    // 属性类型是字符串，通过别名的方式配置扩展插件，常用的插件有：
    // 监控统计用的filter:stat 日志用的filter:log4j 防御sql注入的filter:wall
    private String  filters                                   = "config,stat,wall,log4j2";
    // 连接配置
    private String  connectionProperties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean isPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public int getMaxPoolPreparedStatementPerConnectionSize() {
        return maxPoolPreparedStatementPerConnectionSize;
    }

    public void setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
        this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(String connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

}
