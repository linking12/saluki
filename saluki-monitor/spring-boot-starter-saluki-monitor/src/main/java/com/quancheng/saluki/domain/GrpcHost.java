package com.quancheng.saluki.domain;

import java.io.Serializable;

public class GrpcHost implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            status;

    private String            url;

    private final String      host;

    private final String      httpPort;

    private final String      rpcPort;

    public GrpcHost(String host, String httpPort, String rpcPort){
        this.host = host;
        this.httpPort = httpPort;
        this.rpcPort = rpcPort;
    }

    public GrpcHost(String hostRpcPort, String httpPort){
        String[] hostRpcPort_ = hostRpcPort.split(":");
        this.host = hostRpcPort_[0];
        this.httpPort = httpPort;
        this.rpcPort = hostRpcPort_[1];
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHost() {
        return host;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public String getRpcPort() {
        return rpcPort;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((httpPort == null) ? 0 : httpPort.hashCode());
        result = prime * result + ((rpcPort == null) ? 0 : rpcPort.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        GrpcHost other = (GrpcHost) obj;
        if (host == null) {
            if (other.host != null) return false;
        } else if (!host.equals(other.host)) return false;
        if (httpPort == null) {
            if (other.httpPort != null) return false;
        } else if (!httpPort.equals(other.httpPort)) return false;
        if (rpcPort == null) {
            if (other.rpcPort != null) return false;
        } else if (!rpcPort.equals(other.rpcPort)) return false;
        if (status == null) {
            if (other.status != null) return false;
        } else if (!status.equals(other.status)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SalukiHost [status=" + status + ", url=" + url + ", host=" + host + ", httpPort=" + httpPort
               + ", rpcPort=" + rpcPort + "]";
    }

}
