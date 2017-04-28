/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.grpc.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.quancheng.saluki.gateway.grpc.entity.ApiJarEntity;

/**
 * @author shimingliu 2017年3月24日 下午6:12:58
 * @version ApiJarRepository.java, v 0.0.1 2017年3月24日 下午6:12:58 shimingliu
 */
@Repository
public interface ApiJarRepository extends JpaRepository<ApiJarEntity, String> {

    @Query(value = "SELECT api FROM ApiJarEntity api order by api.id desc")
    List<ApiJarEntity> findLatestJar(Pageable pageable);
}
