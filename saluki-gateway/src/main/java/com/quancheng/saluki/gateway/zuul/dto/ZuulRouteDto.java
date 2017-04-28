/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.zuul.dto;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author shimingliu 2017年3月30日 上午10:06:48
 * @version ZuulRouteDto.java, v 0.0.1 2017年3月30日 上午10:06:48 shimingliu
 */
@Data
@EqualsAndHashCode(of = { "routeId", "routePath" }, callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZuulRouteDto implements Serializable {

    private static final long   serialVersionUID = -7197219166551365479L;

    private String              routeId;

    private String              routePath;

    /**** rest ****/
    /**
     * 这个Id不使用，为了和zuul兼容，但是对于saluki来说没有根据serviceId去查找Service的功能
     */
    private String              serviceId;

    private String              routeUrl;

    private Boolean             stripPrefix;

    private Boolean             retryAble;

    private String              sensitiveHeaders;

    /**** grpc ****/
    private Boolean             isGrpc;

    private String              serviceName;

    private String              group;

    private String              version;

    private String              method;

    private Map<String, String> mappingField;

}
