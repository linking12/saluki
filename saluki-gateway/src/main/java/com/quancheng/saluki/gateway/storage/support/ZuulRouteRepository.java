/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.storage.support;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author shimingliu 2017年3月23日 下午9:02:20
 * @version ZuulRouteRepository.java, v 0.0.1 2017年3月23日 下午9:02:20 shimingliu
 */
@Repository
public interface ZuulRouteRepository extends JpaRepository<ZuulRouteEntity, String> {

}
