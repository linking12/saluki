package com.quancheng.boot.saluki.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "grpc")
public class SalukiProperties {

    /**
     * consumer param
     */
    private String clientHost;

    private String referenceGroup;

    private String referenceVersion;

    /**
     * provider param
     */
    private String serverHost;

    private int    serverStartPort;

    private int    serverRegistryPort;

    private String serviceGroup;

    private String servcieVersion;

    private String consulIp;

    private int    consulPort;

    /**
     * 监控统计时间 (单位为分钟)
     */
    private int    monitorInterval = 30;

    public int getServerStartPort() {
        return serverStartPort;
    }

    public void setServerStartPort(int serverStartPort) {
        this.serverStartPort = serverStartPort;
    }

    public int getServerRegistryPort() {
        return serverRegistryPort;
    }

    public void setServerRegistryPort(int serverRegistryPort) {
        this.serverRegistryPort = serverRegistryPort;
    }

    public String getConsulIp() {
        return consulIp;
    }

    public void setConsulIp(String consulIp) {
        this.consulIp = consulIp;
    }

    public int getConsulPort() {
        return consulPort;
    }

    public void setConsulPort(int consulPort) {
        this.consulPort = consulPort;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public String getServcieVersion() {
        return servcieVersion;
    }

    public void setServcieVersion(String servcieVersion) {
        this.servcieVersion = servcieVersion;
    }

    public String getReferenceGroup() {
        return referenceGroup;
    }

    public void setReferenceGroup(String referenceGroup) {
        this.referenceGroup = referenceGroup;
    }

    public String getReferenceVersion() {
        return referenceVersion;
    }

    public void setReferenceVersion(String referenceVersion) {
        this.referenceVersion = referenceVersion;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public int getMonitorInterval() {
        return monitorInterval;
    }

    public void setMonitorInterval(int monitorInterval) {
        this.monitorInterval = monitorInterval;
    }

}
