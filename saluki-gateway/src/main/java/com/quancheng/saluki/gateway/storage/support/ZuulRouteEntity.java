/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.storage.support;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author shimingliu 2017年3月23日 下午8:45:32
 * @version ZuulRoute.java, v 0.0.1 2017年3月23日 下午8:45:32 shimingliu
 */
@Entity
@Table(name = "zuul_routes")
public class ZuulRouteEntity {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long    id;

    @Column(name = "route_id")
    private String  route_id;

    @Column(name = "path")
    private String  path;

    @Column(name = "service_id")
    private String  service_id;

    @Column(name = "url")
    private String  url;

    @Column(name = "strip_prefix")
    private Boolean strip_prefix;

    @Column(name = "retryable")
    private Boolean retryable;

    @Column(name = "sensitive_headers")
    private String  sensitiveHeaders;

    @Column(name = "is_grpc")
    private Boolean is_grpc;

    @Column(name = "service_name")
    private String  service_name;

    @Column(name = "grpc_group")
    private String  group;

    @Column(name = "grpc_version")
    private String  version;

    @Column(name = "grpc_method")
    private String  method;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoute_id() {
        return route_id;
    }

    public void setRoute_id(String route_id) {
        this.route_id = route_id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getStrip_prefix() {
        return strip_prefix;
    }

    public void setStrip_prefix(Boolean strip_prefix) {
        this.strip_prefix = strip_prefix;
    }

    public Boolean getRetryable() {
        return retryable;
    }

    public void setRetryable(Boolean retryable) {
        this.retryable = retryable;
    }

    public String getSensitiveHeaders() {
        return sensitiveHeaders;
    }

    public void setSensitiveHeaders(String sensitiveHeaders) {
        this.sensitiveHeaders = sensitiveHeaders;
    }

    public Boolean getIs_grpc() {
        return is_grpc;
    }

    public void setIs_grpc(Boolean is_grpc) {
        this.is_grpc = is_grpc;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
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
