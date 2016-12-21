/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.config;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.GrpcEngine;
import com.quancheng.saluki.core.utils.NetUtils;

/**
 * @author shimingliu 2016年12月14日 下午1:34:20
 * @version RpcBaseConfig.java, v 0.0.1 2016年12月14日 下午1:34:20 shimingliu
 */
public class RpcBaseConfig implements Serializable {

    private static final long serialVersionUID = -8963353064840744509L;

    private String            application;

    private String            registryAddress;

    private Integer           registryPort;

    private Integer           monitorinterval;

    private String            host;

    private Integer           realityRpcPort;

    private Integer           registryRpcPort;

    private Integer           httpPort;

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
        return registryPort;
    }

    public void setRegistryPort(Integer registryPort) {
        this.registryPort = registryPort;
    }

    public Integer getMonitorinterval() {
        return monitorinterval;
    }

    public void setMonitorinterval(Integer monitorinterval) {
        this.monitorinterval = monitorinterval;
    }

    public String getHost() {
        String host = this.host;
        Boolean isLocal = NetUtils.isLocalHost(host);
        return (isLocal || StringUtils.isBlank(host)) ? NetUtils.getLocalHost() : host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getRegistryRpcPort() {
        return registryRpcPort;
    }

    public void setRegistryRpcPort(Integer registryRpcPort) {
        this.registryRpcPort = registryRpcPort;
    }

    public Integer getRealityRpcPort() {
        return realityRpcPort;
    }

    public void setRealityRpcPort(Integer realityRpcPort) {
        this.realityRpcPort = realityRpcPort;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    protected void addHttpPort(Map<String, String> params) {
        Integer httpport = getHttpPort();
        if (httpport != null && httpport != 0) {
            params.put(Constants.HTTP_PORT_KEY, httpport.toString());
        }
    }

    protected void addMonitorInterval(Map<String, String> params) {
        Integer monitorInterval = getMonitorinterval();
        if (monitorInterval!=null && monitorInterval != 0) {
            params.put("monitorinterval", monitorInterval.toString());
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((application == null) ? 0 : application.hashCode());
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((httpPort == null) ? 0 : httpPort.hashCode());
        result = prime * result + ((monitorinterval == null) ? 0 : monitorinterval.hashCode());
        result = prime * result + ((realityRpcPort == null) ? 0 : realityRpcPort.hashCode());
        result = prime * result + ((registryAddress == null) ? 0 : registryAddress.hashCode());
        result = prime * result + ((registryPort == null) ? 0 : registryPort.hashCode());
        result = prime * result + ((registryRpcPort == null) ? 0 : registryRpcPort.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        RpcBaseConfig other = (RpcBaseConfig) obj;
        if (application == null) {
            if (other.application != null) return false;
        } else if (!application.equals(other.application)) return false;
        if (host == null) {
            if (other.host != null) return false;
        } else if (!host.equals(other.host)) return false;
        if (httpPort == null) {
            if (other.httpPort != null) return false;
        } else if (!httpPort.equals(other.httpPort)) return false;
        if (monitorinterval == null) {
            if (other.monitorinterval != null) return false;
        } else if (!monitorinterval.equals(other.monitorinterval)) return false;
        if (realityRpcPort == null) {
            if (other.realityRpcPort != null) return false;
        } else if (!realityRpcPort.equals(other.realityRpcPort)) return false;
        if (registryAddress == null) {
            if (other.registryAddress != null) return false;
        } else if (!registryAddress.equals(other.registryAddress)) return false;
        if (registryPort == null) {
            if (other.registryPort != null) return false;
        } else if (!registryPort.equals(other.registryPort)) return false;
        if (registryRpcPort == null) {
            if (other.registryRpcPort != null) return false;
        } else if (!registryRpcPort.equals(other.registryRpcPort)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "RpcBaseConfig [application=" + application + ", registryAddress=" + registryAddress + ", registryPort="
               + registryPort + ", monitorinterval=" + monitorinterval + ", host=" + host + ", realityRpcPort="
               + realityRpcPort + ", registryRpcPort=" + registryRpcPort + ", httpPort=" + httpPort + "]";
    }

    public GrpcEngine getGrpcEngine() {
        return GrpcEngineHolder.getEngine(registryAddress, registryPort);
    }

    private static class GrpcEngineHolder {

        private static final ReentrantLock           LOCK    = new ReentrantLock();

        private static final Map<String, GrpcEngine> ENGINES = Maps.newConcurrentMap();

        private static class GrpcEngineHolderSingleton {

            private static final GrpcEngineHolder INSTANCE = new GrpcEngineHolder();
        }

        private GrpcEngineHolder(){
        }

        private GrpcEngine getGrpcEngine(String registryAddress, int registryPort) {
            String key = registryAddress + ":" + registryPort;
            LOCK.lock();
            try {
                GrpcEngine engine = ENGINES.get(key);
                if (engine != null) {
                    return engine;
                }
                Preconditions.checkNotNull(registryAddress, "registryAddress  is not Null", registryAddress);
                Preconditions.checkState(registryPort != 0, "RegistryPort can not be zero", registryPort);
                GrpcURL registryUrl = new GrpcURL(Constants.REGISTRY_PROTOCOL, registryAddress, registryPort);
                engine = new GrpcEngine(registryUrl);
                ENGINES.put(key, engine);
                return engine;
            } finally {
                LOCK.unlock();
            }
        }

        public static final GrpcEngine getEngine(String registryAddress, Integer registryPort) {
            return GrpcEngineHolderSingleton.INSTANCE.getGrpcEngine(registryAddress, registryPort);
        }

    }

}
