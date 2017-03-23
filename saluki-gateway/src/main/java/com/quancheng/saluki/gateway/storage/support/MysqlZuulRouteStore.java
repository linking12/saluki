/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.storage.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author shimingliu 2017年3月23日 上午9:46:40
 * @version MysqlZuulRouteStore.java, v 0.0.1 2017年3月23日 上午9:46:40 shimingliu
 */
public class MysqlZuulRouteStore implements ZuulRouteStore {

    private JdbcTemplate jdbcTemplate;

    public MysqlZuulRouteStore(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ZuulRoute> findAll() {
        List<ZuulRoute> list = jdbcTemplate.query("select * from zuul_routes", new RowMapper<ZuulRoute>() {

            public ZuulRoute mapRow(ResultSet rs, int rowNum) throws SQLException {
                String id = rs.getString(1);
                String path = rs.getString(2);
                String service_id = rs.getString(3);
                String url = rs.getString(4);
                boolean strip_prefix = rs.getBoolean(5);
                boolean retryable = rs.getBoolean(6);
                String sensitiveHeader = rs.getString(7);
                String[] sensitiveHeaders = null;
                if (sensitiveHeader != null) {
                    sensitiveHeaders = StringUtils.split(sensitiveHeader, ",");
                } else {
                    sensitiveHeaders = new String[] {};
                }
                return new ZuulRoute(id, path, service_id, url, strip_prefix, retryable,
                                     new HashSet<String>(Arrays.asList(sensitiveHeaders)));
            }

        });
        return list;
    }

}
