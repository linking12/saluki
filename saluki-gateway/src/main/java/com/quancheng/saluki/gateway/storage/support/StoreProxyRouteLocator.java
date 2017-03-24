/**
 * Copyright (c) 2015 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.saluki.gateway.storage.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.gateway.storage.RouterLocalCache;

/**
 * A simple {@link org.springframework.cloud.netflix.zuul.filters.RouteLocator} that is being populated from configured
 * {@link ZuulRouteStore}.
 *
 * @author Jakub Narloch
 */
public class StoreProxyRouteLocator extends DiscoveryClientRouteLocator {

    private final Logger                   log             = LoggerFactory.getLogger(StoreProxyRouteLocator.class);

    private final ZuulRouteRepository      store;

    private final ScheduledExecutorService refreshExecutor = Executors.newScheduledThreadPool(1,
                                                                                              new NamedThreadFactory("refreshZuulRoute",
                                                                                                                     true));;

    /**
     * Creates new instance of {@link StoreProxyRouteLocator}
     * 
     * @param servletPath the application servlet path
     * @param discovery the discovery service
     * @param properties the zuul properties
     * @param store the route store
     */
    public StoreProxyRouteLocator(String servletPath, DiscoveryClient discovery, ZuulProperties properties,
                                  ZuulRouteRepository store){
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
    public void refresh() {
        doRefresh();
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addConfiguredRoutes(Map<String, ZuulProperties.ZuulRoute> routes) {
        super.addConfiguredRoutes(routes);
    }

    private List<ZuulProperties.ZuulRoute> findAll() {
        List<ZuulRouteEntity> routers = store.findAll();
        RouterLocalCache.getInstance().putAllRouters(routers);
        return Lists.transform(routers, new Function<ZuulRouteEntity, ZuulProperties.ZuulRoute>() {

            @Override
            public ZuulRoute apply(ZuulRouteEntity input) {
                String id = input.getRoute_id();
                String path = input.getPath();
                String service_id = input.getService_id();
                String url = input.getUrl();
                boolean strip_prefix = input.getStrip_prefix() != null ? input.getStrip_prefix() : true;
                Boolean retryable = input.getRetryable();
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
