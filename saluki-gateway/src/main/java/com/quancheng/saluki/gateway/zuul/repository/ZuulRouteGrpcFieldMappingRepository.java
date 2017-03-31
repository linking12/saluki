/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.zuul.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quancheng.saluki.gateway.zuul.entity.ZuulGrpcFieldMappingEntity;

/**
 * @author shimingliu 2017年3月31日 下午4:32:28
 * @version ZuulRouteGrpcFieldMappingRepository.java, v 0.0.1 2017年3月31日 下午4:32:28 shimingliu
 */
public interface ZuulRouteGrpcFieldMappingRepository extends JpaRepository<ZuulGrpcFieldMappingEntity, Long> {

}
