/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.router;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.router.internal.ConditionRouter;
import com.quancheng.saluki.core.grpc.router.internal.ScriptRouter;

/**
 * @author shimingliu 2016年12月28日 下午8:33:41
 * @version GrpcRouterFactory.java, v 0.0.1 2016年12月28日 下午8:33:41 shimingliu
 */
public final class GrpcRouterFactory {

  private static final LoadingCache<String, String> ROUTE_CACHE = CacheBuilder.newBuilder() //
      .concurrencyLevel(8) //
      .initialCapacity(10) //
      .maximumSize(100) //
      .recordStats() //
      .build(new CacheLoader<String, String>() {

        @Override
        public String load(String key) throws Exception {
          return null;
        }

      });
  private static final GrpcRouterFactory instance = new GrpcRouterFactory();

  private GrpcRouterFactory() {}

  public static GrpcRouterFactory getInstance() {
    return instance;
  }

  public void cacheRoute(String group, String routerCondition) {
    if (!StringUtils.isEmpty(routerCondition)) {
      ROUTE_CACHE.put(group, routerCondition);
    } else {
      ROUTE_CACHE.invalidate(group);
    }
  }

  public GrpcRouter getGrpcRouter(String group) {
    String currentRouterRule = null;
    // 从线程上下文取路由规则
    if (RpcContext.getContext().containAttachment("routerRule")) {
      currentRouterRule = RpcContext.getContext().getAttachment("routerRule");
    }
    // 从配置中心获取路由规则并覆盖线程上下文的路由规则
    String configRouterRule = ROUTE_CACHE.getIfPresent(group);
    if (configRouterRule != null) {
      currentRouterRule = configRouterRule;
    }
    if (currentRouterRule != null) {
      try {
        return this.createRouter(currentRouterRule);
      } finally {
        RpcContext.getContext().removeAttachment("routerRule");
      }
    } else {
      return null;
    }
  }

  private GrpcRouter createRouter(String routerMessage) {
    if (routerMessage.startsWith("condition://") || routerMessage.indexOf("//") == -1) {
      routerMessage = routerMessage.replaceAll("condition://", "");
      return new ConditionRouter(routerMessage);
    } else {
      String[] router = StringUtils.split(routerMessage, "://");
      if (router.length == 2) {
        String type = router[0];
        String routerScript = router[1];
        return new ScriptRouter(type, routerScript);
      }
      throw new IllegalStateException("No router type for script");
    }
  }
}
