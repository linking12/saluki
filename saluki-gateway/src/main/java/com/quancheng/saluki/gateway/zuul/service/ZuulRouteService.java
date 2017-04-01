/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.zuul.service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.quancheng.saluki.gateway.zuul.dto.ZuulRouteDto;
import com.quancheng.saluki.gateway.zuul.entity.ZuulGrpcFieldMappingEntity;
import com.quancheng.saluki.gateway.zuul.entity.ZuulRouteEntity;
import com.quancheng.saluki.gateway.zuul.repository.ZuulRouteRepository;

/**
 * @author shimingliu 2017年3月30日 下午8:40:42
 * @version ZuulRouteService.java, v 0.0.1 2017年3月30日 下午8:40:42 shimingliu
 */
@Service
public class ZuulRouteService {

    @Autowired
    private ZuulRouteRepository zuulRouteRepository;

    public List<ZuulRouteDto> loadAllRoute() {
        return zuulRouteRepository.findAll().stream().map(entity2Dto)//
                                  .collect(Collectors.toList());
    }

    public List<ZuulRouteDto> loadTop10Route() {
        return zuulRouteRepository.findTop10Route(new PageRequest(0, 10)).stream().map(entity2Dto)//
                                  .collect(Collectors.toList());
    }

    Function<ZuulRouteEntity, ZuulRouteDto> entity2Dto = zuulRouteEntity -> ZuulRouteDto.builder()//
                                                                                        .routeId(zuulRouteEntity.getZuul_route_id())//
                                                                                        .routePath(zuulRouteEntity.getPath())//
                                                                                        .serviceId(zuulRouteEntity.getService_id())//
                                                                                        .routeUrl(zuulRouteEntity.getUrl())//
                                                                                        .stripPrefix(zuulRouteEntity.getStrip_prefix())//
                                                                                        .retryAble(zuulRouteEntity.getRetryable())//
                                                                                        .sensitiveHeaders(zuulRouteEntity.getSensitiveHeaders())//
                                                                                        .isGrpc(zuulRouteEntity.getIs_grpc())//
                                                                                        .serviceName(zuulRouteEntity.getService_name())//
                                                                                        .group(zuulRouteEntity.getGroup())//
                                                                                        .version(zuulRouteEntity.getVersion())//
                                                                                        .method(zuulRouteEntity.getMethod())//
                                                                                        .mappingField(zuulRouteEntity.getFieldMapping()//
                                                                                                                     .stream()//
                                                                                                                     .collect(Collectors.toMap(ZuulGrpcFieldMappingEntity::getSourceField,
                                                                                                                                               ZuulGrpcFieldMappingEntity::getTargetField)))//
                                                                                        .build();
}
