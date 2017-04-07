/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.zuul.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quancheng.saluki.gateway.zuul.entity.ZuulRouteEntity;

/**
 * @author shimingliu 2017年3月23日 下午9:02:20
 * @version ZuulRouteRepository.java, v 0.0.1 2017年3月23日 下午9:02:20 shimingliu
 */
@Repository
public interface ZuulRouteRepository extends JpaRepository<ZuulRouteEntity, String> {

    @Query(value = "select route from ZuulRouteEntity route order by route.id desc")
    public List<ZuulRouteEntity> findTop10Route(Pageable pageable);

    @Query(value = "select route from ZuulRouteEntity route where route.is_grpc=0 or route.is_grpc=null order by route.id desc")
    public Page<ZuulRouteEntity> findAllRest(Pageable pageable);

    @Query(value = "select route from ZuulRouteEntity route where route.is_grpc=1 order by route.id desc")
    public Page<ZuulRouteEntity> findAllGrpc(Pageable pageable);

    @Query(value = "select route from ZuulRouteEntity route where route.zuul_route_id = :route_Id")
    Optional<ZuulRouteEntity> findOneByRouteId(@Param("route_Id") String route_Id);

}
