/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.web.dto;

import java.io.Serializable;

/**
 * @author shimingliu 2017年3月30日 上午10:06:48
 * @version ZuulRouteDto.java, v 0.0.1 2017年3月30日 上午10:06:48 shimingliu
 */
public class ZuulRouteDto implements Serializable {

    private static final long serialVersionUID = -7197219166551365479L;

    private String            routeId;

    private String            routePath;

    /**** rest ****/
    /**
     * 这个Id不使用，为了和zuul兼容，但是对于saluki来说没有根据serviceId去查找Service的功能
     */
    private String            serviceId;

    private String            routeUrl;

    private String            stripPrefix;

    private Boolean           retryAble;

    private String            sensitiveHeaders;

    /**** grpc ****/
    private Boolean           isGrpc;

    private String            serviceName;

    private String            group;

    private String            version;

    private String            method;

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getRouteUrl() {
        return routeUrl;
    }

    public void setRouteUrl(String routeUrl) {
        this.routeUrl = routeUrl;
    }

    public String getStripPrefix() {
        return stripPrefix;
    }

    public void setStripPrefix(String stripPrefix) {
        this.stripPrefix = stripPrefix;
    }

    public Boolean getRetryAble() {
        return retryAble;
    }

    public void setRetryAble(Boolean retryAble) {
        this.retryAble = retryAble;
    }

    public String getSensitiveHeaders() {
        return sensitiveHeaders;
    }

    public void setSensitiveHeaders(String sensitiveHeaders) {
        this.sensitiveHeaders = sensitiveHeaders;
    }

    public Boolean getIsGrpc() {
        return isGrpc;
    }

    public void setIsGrpc(Boolean isGrpc) {
        this.isGrpc = isGrpc;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

}
