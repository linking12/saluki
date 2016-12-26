/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.registry.consul;

/**
 * @author shimingliu 2016年12月16日 上午10:31:30
 * @version ConsulConstants.java, v 0.0.1 2016年12月16日 上午10:31:30 shimingliu
 */
public interface ConsulConstants {

    /**
     * service 最长存活周期（Time To Live），单位秒。 每个service会注册一个ttl类型的check，在最长TTL秒不发送心跳 就会将service变为不可用状态。
     */
    public static int  TTL                       = 30;

    /**
     * 心跳周期，取ttl的2/3
     */
    public static int  HEARTBEAT_CIRCLE          = (TTL * 1000 * 2) / 3 / 10;

    /**
     * consul服务查询默认间隔时间。单位毫秒
     */
    public static int  DEFAULT_LOOKUP_INTERVAL   = 30000;

    /**
     * consul block 查询时 block的最长时间,单位，分钟
     */
    public static int  CONSUL_BLOCK_TIME_MINUTES = 10;

    /**
     * consul block 查询时 block的最长时间,单位，秒
     */
    public static long CONSUL_BLOCK_TIME_SECONDS = CONSUL_BLOCK_TIME_MINUTES * 60;
}
