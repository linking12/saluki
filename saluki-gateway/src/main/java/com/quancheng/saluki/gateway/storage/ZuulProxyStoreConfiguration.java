/**
 * Copyright (c) 2015 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.saluki.gateway.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.ZuulProxyConfiguration;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.cloud.netflix.zuul.web.ZuulController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.quancheng.saluki.gateway.filters.route.GrpcRemoteController;
import com.quancheng.saluki.gateway.grpc.GrpcRemoteComponent;
import com.quancheng.saluki.gateway.storage.support.StoreProxyRouteLocator;
import com.quancheng.saluki.gateway.storage.support.ZuulRouteRepository;

/***
 * Registers a{
 * 
 * @link org.springframework.cloud.netflix.zuul.filters.RouteLocator} that is being populated through external store.
 * @author Jakub Narloch
 */
@Configuration
public class ZuulProxyStoreConfiguration extends ZuulProxyConfiguration {

    @Autowired
    private ZuulRouteRepository zuulRouteStore;

    @Autowired
    private DiscoveryClient     discovery;

    @Autowired
    private ZuulProperties      zuulProperties;

    @Autowired
    private ServerProperties    server;

    @Autowired
    private GrpcRemoteComponent grpcComponet;

    @Bean
    @Override
    @ConditionalOnMissingBean(RouteLocator.class)
    public DiscoveryClientRouteLocator routeLocator() {
        return new StoreProxyRouteLocator(server.getServletPath(), discovery, zuulProperties, zuulRouteStore);
    }

    @Bean
    public ZuulController zuulController() {
        return new GrpcRemoteController(grpcComponet);
    }
}
