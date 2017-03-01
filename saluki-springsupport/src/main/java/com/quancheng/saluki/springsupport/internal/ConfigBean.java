/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.springsupport.internal;

/**
 * @author shimingliu 2017年2月28日 下午5:38:57
 * @version ConfigBean.java, v 0.0.1 2017年2月28日 下午5:38:57 shimingliu
 */
public class ConfigBean {

    private String  application;

    private String  registryAddress;

    private Integer registryPort;

    private Integer monitorinterval;

    private String  host;

    private Integer realityRpcPort;

    private Integer registryRpcPort;

    private Integer httpPort;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public Integer getRegistryPort() {
        if (registryPort != null) {
            return registryPort;
        } else {
            return 0;
        }
    }

    public void setRegistryPort(Integer registryPort) {
        this.registryPort = registryPort;
    }

    public Integer getMonitorinterval() {
        if (monitorinterval != null) {
            return monitorinterval;
        } else {
            return 0;
        }
    }

    public void setMonitorinterval(Integer monitorinterval) {
        this.monitorinterval = monitorinterval;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getRealityRpcPort() {
        if (realityRpcPort != null) {
            return realityRpcPort;
        } else {
            return 0;
        }
    }

    public void setRealityRpcPort(Integer realityRpcPort) {
        this.realityRpcPort = realityRpcPort;
    }

    public Integer getRegistryRpcPort() {
        if (registryRpcPort != null) {
            return registryRpcPort;
        } else {
            return 0;
        }
    }

    public void setRegistryRpcPort(Integer registryRpcPort) {
        this.registryRpcPort = registryRpcPort;
    }

    public Integer getHttpPort() {
        if (httpPort != null) {
            return httpPort;
        } else {
            return 0;
        }
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

}
