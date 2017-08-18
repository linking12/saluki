/**
 * Copyright (c) 2015 the original author or authors
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
package com.quancheng.saluki.gateway.zuul.extend;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.gateway.zuul.dto.ZuulRouteDto;
import com.quancheng.saluki.gateway.zuul.service.ZuulRouteService;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author liushiming
 * @version StoreProxyRouteLocator.java, v 0.0.1 2017年8月14日 下午4:52:01 liushiming
 * @since JDK 1.8
 */
@Slf4j
public class StoreProxyRouteLocator extends DiscoveryClientRouteLocator {


  private final ZuulRouteService store;

  private volatile Boolean hasLoaded = false;

  private final ScheduledExecutorService refreshExecutor =
      Executors.newScheduledThreadPool(1, new NamedThreadFactory("refreshZuulRoute", true));;


  /**
   * Creates new instance of {@link StoreProxyRouteLocator}
   * 
   * @param servletPath the application servlet path
   * @param discovery the discovery service
   * @param properties the zuul properties
   * @param store the route store
   */
  public StoreProxyRouteLocator(String servletPath, DiscoveryClient discovery,
      ZuulProperties properties, ZuulRouteService store) {
    super(servletPath, discovery, properties);
    this.store = store;
    refreshExecutor.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        try {
          refresh();
        } catch (Throwable e) {
          log.error(e.getMessage(), e);
        }
      }
    }, 0, 1, TimeUnit.MINUTES);
  }

  @Override
  public Collection<String> getIgnoredPaths() {
    List<String> ignoredPath = Lists.newArrayList();
    ignoredPath.add("/oauth/**");
    return ignoredPath;
  }

  @Override
  protected LinkedHashMap<String, ZuulRoute> locateRoutes() {
    LinkedHashMap<String, ZuulRoute> routesMap = new LinkedHashMap<String, ZuulRoute>();
    routesMap.putAll(super.locateRoutes());
    for (ZuulProperties.ZuulRoute route : findAll()) {
      routesMap.put(route.getPath(), route);
    }
    return routesMap;
  }

  private Set<ZuulRouteDto> routesCache = Sets.newConcurrentHashSet();

  @Override
  protected void addConfiguredRoutes(Map<String, ZuulProperties.ZuulRoute> routes) {
    super.addConfiguredRoutes(routes);
  }

  public Set<ZuulRouteDto> loadRoutes() {
    return routesCache;
  }

  private List<ZuulProperties.ZuulRoute> findAll() {
    Set<ZuulRouteDto> routers = Sets.newHashSet();
    if (!hasLoaded) {
      routers.addAll(store.loadAllRoute());
      hasLoaded = true;
    } else {
      routers.addAll(store.loadTop10Route());
    }
    routesCache.addAll(routers);
    return Lists.transform(Lists.newArrayList(routesCache),
        new Function<ZuulRouteDto, ZuulProperties.ZuulRoute>() {

          @Override
          public ZuulRoute apply(ZuulRouteDto input) {
            String id = input.getRouteId();
            String path = input.getRoutePath();
            String service_id = input.getServiceId();
            String url = input.getRouteUrl();
            boolean strip_prefix = input.getStripPrefix() != null ? input.getStripPrefix() : true;
            Boolean retryable = input.getRetryAble();
            String sensitiveHeader = input.getSensitiveHeaders();
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
  }
}
