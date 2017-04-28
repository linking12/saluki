/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.zuul;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.google.common.collect.Sets;
import com.quancheng.saluki.gateway.zuul.entity.ZuulGrpcFieldMappingEntity;
import com.quancheng.saluki.gateway.zuul.entity.ZuulRouteEntity;
import com.quancheng.saluki.gateway.zuul.repository.ZuulRouteRepository;

/**
 * @author shimingliu 2017年4月1日 上午11:41:17
 * @version DefaultZuulRouteConfiguration.java, v 0.0.1 2017年4月1日 上午11:41:17 shimingliu
 */
@Configuration
@Profile("default-user-and-roles_route")
public class DefaultZuulRouteConfiguration implements InitializingBean {

    @Autowired
    private ZuulRouteRepository routeRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (routeRepository.count() == 0) {
            ZuulRouteEntity entityGrpc = ZuulRouteEntity.builder()//
                                                        .zuul_route_id("helloGrpc")//
                                                        .path("/api/helloGrpc")//
                                                        .is_grpc(true)//
                                                        .service_name("com.quancheng.examples.service.HelloService")//
                                                        .method("sayHello")//
                                                        .group("example")//
                                                        .version("1.0.0")//
                                                        .build();

            ZuulRouteEntity entityRest = ZuulRouteEntity.builder()//
                                                        .zuul_route_id("helloRest")//
                                                        .path("/api/helloRest")//
                                                        .strip_prefix(false)//
                                                        .url("http://www.baidu.com")//
                                                        .build();

            ZuulGrpcFieldMappingEntity fieldMapping = ZuulGrpcFieldMappingEntity.builder()//
                                                                                .sourceField("name")//
                                                                                .targetField("name")//
                                                                                .targetFieldType("string").build();
            fieldMapping.setRoute(entityGrpc);
            entityGrpc.setFieldMapping(Sets.newHashSet(fieldMapping));
            routeRepository.save(entityGrpc);
            routeRepository.save(entityRest);
        }

    }

}
