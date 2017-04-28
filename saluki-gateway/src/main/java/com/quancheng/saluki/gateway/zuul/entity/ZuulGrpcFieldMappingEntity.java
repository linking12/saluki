/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.zuul.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author shimingliu 2017年3月31日 下午4:22:40
 * @version FieldMappingEntity.java, v 0.0.1 2017年3月31日 下午4:22:40 shimingliu
 */

@Data
@EqualsAndHashCode(of = { "sourceField", "targetField" }, callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "zuul_grpc_route_fieldmapping")
public class ZuulGrpcFieldMappingEntity extends AbstractPersistable<Long> {

    @NotNull
    @Column(name = "source_field", nullable = false, length = 100)
    private String          sourceField;

    @NotNull
    @Column(name = "target_field", nullable = false, length = 100)
    private String          targetField;

    @Column(name = "target_field_type", length = 100)
    private String          targetFieldType;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = { CascadeType.DETACH, CascadeType.REFRESH })
    @JoinColumn(name = "route_id")
    private ZuulRouteEntity route;

}
