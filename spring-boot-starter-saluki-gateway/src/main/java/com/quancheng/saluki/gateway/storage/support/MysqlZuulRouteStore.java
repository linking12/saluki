/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.storage.support;

import java.util.List;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;

/**
 * @author shimingliu 2017年3月23日 上午9:46:40
 * @version MysqlZuulRouteStore.java, v 0.0.1 2017年3月23日 上午9:46:40 shimingliu
 */
public class MysqlZuulRouteStore implements ZuulRouteStore {

    @Override
    public List<ZuulRoute> findAll() {
        return null;
    }

}
