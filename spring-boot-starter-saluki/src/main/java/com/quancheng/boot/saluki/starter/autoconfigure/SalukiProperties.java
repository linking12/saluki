package com.quancheng.boot.saluki.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "grpc")
public class SalukiProperties {

    private String application;
    /**
     * consumer param
     */
    private String clientHost;

    private String referenceDefinition;

    /**
     * provider param
     */
    private String serverHost;

    private int    serverStartPort;

    private int    serverRegistryPort;

    private String serviceGroup;

    private String servcieVersion;

    /**
     * consul注册中心
     */
    private String consulIp;

    private int    consulPort;

    /**
     * 监控统计时间 (单位为分钟)
     */
    private String monitorInterval;

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

    public String getMonitorInterval() {
        return monitorInterval;
    }

    public void setMonitorInterval(String monitorInterval) {
        this.monitorInterval = monitorInterval;
    }

    public String getReferenceDefinition() {
        return referenceDefinition;
    }

    public void setReferenceDefinition(String referenceDefinition) {
        this.referenceDefinition = referenceDefinition;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

}
