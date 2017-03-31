/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.oauth2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author shimingliu 2017年3月31日 下午1:33:21
 * @version UserLimitEntity.java, v 0.0.1 2017年3月31日 下午1:33:21 shimingliu
 */

@Data
@EqualsAndHashCode(of = { "intervalInMills", "limits" }, callSuper = true)
@ToString(of = { "intervalInMills", "limits" }, callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_limit")
public class UserLimitEntity extends AbstractPersistable<Long> {

    @Column(name = "intervalInMills")
    private Long       intervalInMills;

    @Column(name = "limits")
    private Long       limits;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
