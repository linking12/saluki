/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.zuul.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author shimingliu 2017年3月23日 下午8:45:32
 * @version ZuulRoute.java, v 0.0.1 2017年3月23日 下午8:45:32 shimingliu
 */

@Data
@EqualsAndHashCode(of = { "route_id", "path" }, callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "zuul_routes")
public class ZuulRouteEntity extends AbstractPersistable<Long> {

    @Column(name = "zuul_route_id", unique = true, nullable = false, length = 200)
    private String                          zuul_route_id;

    @Column(name = "path")
    private String                          path;

    @Column(name = "service_id")
    private String                          service_id;

    @Column(name = "url")
    private String                          url;

    @Column(name = "strip_prefix")
    private Boolean                         strip_prefix;

    @Column(name = "retryable")
    private Boolean                         retryable;

    @Column(name = "sensitive_headers")
    private String                          sensitiveHeaders;

    @Column(name = "is_grpc")
    private Boolean                         is_grpc;

    @Column(name = "service_name")
    private String                          service_name;

    @Column(name = "grpc_group")
    private String                          group;

    @Column(name = "grpc_version")
    private String                          version;

    @Column(name = "grpc_method")
    private String                          method;

    @OneToMany(mappedBy = "route", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ZuulGrpcFieldMappingEntity> fieldMapping;

}
