package com.quancheng.boot.saluki.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "grpc")
public class SalukiProperties {

    private int    serverPort = 6565;

    private String consulIp;

    private int    consulPort;

    private String referenceGroup;

    private String referenceVersion;

    private String serviceGroup;

    private String servcieVersion;

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
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

}
