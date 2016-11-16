package com.quancheng.saluki.monitor.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class SalukiHost implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String      host;

    private final String      httpPort;

    private final String      rpcPort;

    public SalukiHost(String hostRpcPort){
        String[] _hostPort = StringUtils.split(hostRpcPort, ":");
        if (_hostPort.length > 1) {
            this.host = _hostPort[0];
            this.rpcPort = _hostPort[1];
        } else {
            this.host = _hostPort[0];
            this.rpcPort = null;
        }
        this.httpPort = null;
    }

    public SalukiHost(String host, String httpPort, String rpcPort){
        this.host = host;
        this.httpPort = httpPort;
        this.rpcPort = rpcPort;
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
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SalukiHost other = (SalukiHost) obj;
        if (host == null) {
            if (other.host != null) return false;
        } else if (!host.equals(other.host)) return false;
        if (httpPort == null) {
            if (other.httpPort != null) return false;
        } else if (!httpPort.equals(other.httpPort)) return false;
        if (rpcPort == null) {
            if (other.rpcPort != null) return false;
        } else if (!rpcPort.equals(other.rpcPort)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SalukiHost [host=" + host + ", httpPort=" + httpPort + ", rpcPort=" + rpcPort + "]";
    }

}
