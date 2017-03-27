/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author shimingliu 2016年12月16日 下午4:58:23
 * @version ThrallProperties.java, v 0.0.1 2016年12月16日 下午4:58:23 shimingliu
 */

@ConfigurationProperties(prefix = "saluki.grpc")
public class GrpcProperties {

    /**
     * consumer
     */
    private String referenceDefinition;

    /**
     * provider
     */
    private String group;

    private String version;

    private int    realityRpcPort;

    private int    registryRpcPort;

    /**
     * commom
     */
    private String host;

    private int    registryHttpPort;

    private int    monitorinterval;

    private String registryAddress;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getRealityRpcPort() {
        return realityRpcPort;
    }

    public void setRealityRpcPort(int realityRpcPort) {
        this.realityRpcPort = realityRpcPort;
    }

    public int getRegistryRpcPort() {
        return registryRpcPort;
    }

    public void setRegistryRpcPort(int registryRpcPort) {
        this.registryRpcPort = registryRpcPort;
    }

    public void setRegistryHttpPort(int registryHttpPort) {
        this.registryHttpPort = registryHttpPort;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getReferenceDefinition() {
        return referenceDefinition;
    }

    public void setReferenceDefinition(String referenceDefinition) {
        this.referenceDefinition = referenceDefinition;
    }

    public int getRegistryHttpPort() {
        return registryHttpPort;
    }

    public int getMonitorinterval() {
        return monitorinterval;
    }

    public void setMonitorinterval(int monitorinterval) {
        this.monitorinterval = monitorinterval;
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

}
