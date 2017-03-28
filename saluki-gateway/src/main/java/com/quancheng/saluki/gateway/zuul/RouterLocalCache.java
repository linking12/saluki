/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.zuul;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import com.quancheng.saluki.gateway.zuul.support.ZuulRouteEntity;

/**
 * @author shimingliu 2017年3月24日 下午6:35:47
 * @version RouterLocalCache.java, v 0.0.1 2017年3月24日 下午6:35:47 shimingliu
 */
public class RouterLocalCache {

    private static final Logger                                      logger             = LoggerFactory.getLogger(RouterLocalCache.class);

    private static final String                                      GATE_WAY_CACHE_KEY = "GATEWAY_ROUTER";

    private static final LoadingCache<String, List<ZuulRouteEntity>> routerCache        = CacheBuilder.newBuilder()                                                                               //
                                                                                                      .concurrencyLevel(8)                                                                        //
                                                                                                      .expireAfterWrite(1,
                                                                                                                        TimeUnit.DAYS)                                                            //
                                                                                                      .initialCapacity(10)                                                                        //
                                                                                                      .maximumSize(100)                                                                           //
                                                                                                      .recordStats()                                                                              //
                                                                                                      .removalListener(new RemovalListener<String, List<ZuulRouteEntity>>() {

                                                                                                          @Override
                                                                                                          public void onRemoval(RemovalNotification<String, List<ZuulRouteEntity>> notification) {
                                                                                                              logger.info("remove key:"
                                                                                                                          + notification.getKey()
                                                                                                                          + ",value:"
                                                                                                                          + notification.getValue());
                                                                                                          }
                                                                                                      })                                                                                          //
                                                                                                      .build(new CacheLoader<String, List<ZuulRouteEntity>>() {

                                                                                                          @Override
                                                                                                          public List<ZuulRouteEntity> load(String key) throws Exception {
                                                                                                              return Lists.newArrayList();
                                                                                                          }

                                                                                                      });

    private static class LazyHolder {

        private static final RouterLocalCache INSTANCE = new RouterLocalCache();
    }

    private RouterLocalCache(){
    }

    public static final RouterLocalCache getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void putAllRouters(List<ZuulRouteEntity> routers) {
        routerCache.put(GATE_WAY_CACHE_KEY, routers);
    }

    public List<ZuulRouteEntity> getRouters() {
        try {
            return routerCache.get(GATE_WAY_CACHE_KEY);
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
            return Lists.newArrayList();
        }
    }
}
