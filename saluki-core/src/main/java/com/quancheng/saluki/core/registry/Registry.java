/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.registry;

import java.util.List;

import com.quancheng.saluki.core.common.GrpcURL;

/**
 * @author shimingliu 2016年12月14日 下午1:46:29
 * @version Registry.java, v 0.0.1 2016年12月14日 下午1:46:29 shimingliu
 */
public interface Registry {

    /**
     * 注册服务
     */
    void register(GrpcURL url);

    /**
     * 取消注册
     */
    void unregister(GrpcURL url);

    /**
     * 订阅服务
     */
    void subscribe(GrpcURL url, NotifyListener.NotifyServiceListener listener);

    /**
     * 订阅服务路由信息
     */
    void subscribe(String group, NotifyListener.NotifyRouterListener listener);

    /**
     * 取消订阅
     */
    void unsubscribe(GrpcURL url, NotifyListener.NotifyServiceListener listener);

    /**
     * 取消订阅路由信息
     */
    void unsubscribe(String group, NotifyListener.NotifyRouterListener listener);

    /**
     * 查询服务
     */
    List<GrpcURL> discover(GrpcURL url);

}
