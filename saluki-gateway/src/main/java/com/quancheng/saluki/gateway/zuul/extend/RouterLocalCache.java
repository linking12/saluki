/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.zuul.extend;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Sets;
import com.quancheng.saluki.gateway.zuul.dto.ZuulRouteDto;

/**
 * @author shimingliu 2017年3月24日 下午6:35:47
 * @version RouterLocalCache.java, v 0.0.1 2017年3月24日 下午6:35:47 shimingliu
 */
public class RouterLocalCache {

    private static final Logger                                  logger             = LoggerFactory.getLogger(RouterLocalCache.class);

    private static final String                                  GATE_WAY_CACHE_KEY = "GATEWAY_ROUTER";

    private static final LoadingCache<String, Set<ZuulRouteDto>> routerCache        = CacheBuilder.newBuilder()                                                                           //
                                                                                                  .concurrencyLevel(8)                                                                    //
                                                                                                  .expireAfterWrite(1,
                                                                                                                    TimeUnit.DAYS)                                                        //
                                                                                                  .initialCapacity(10)                                                                    //
                                                                                                  .maximumSize(100)                                                                       //
                                                                                                  .recordStats()                                                                          //
                                                                                                  .removalListener(new RemovalListener<String, Set<ZuulRouteDto>>() {

                                                                                                      @Override
                                                                                                      public void onRemoval(RemovalNotification<String, Set<ZuulRouteDto>> notification) {

                                                                                                      }
                                                                                                  })                                                                                      //
                                                                                                  .build(new CacheLoader<String, Set<ZuulRouteDto>>() {

                                                                                                      @Override
                                                                                                      public Set<ZuulRouteDto> load(String key) throws Exception {
                                                                                                          return Sets.newHashSet();
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

    public void putAllRouters(Set<ZuulRouteDto> routers) {
        routerCache.put(GATE_WAY_CACHE_KEY, routers);
    }

    public Set<ZuulRouteDto> getRouters() {
        try {
            return routerCache.get(GATE_WAY_CACHE_KEY);
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
            return Sets.newHashSet();
        }
    }
}
