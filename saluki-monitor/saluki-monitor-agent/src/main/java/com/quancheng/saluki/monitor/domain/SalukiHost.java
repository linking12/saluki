package com.quancheng.saluki.monitor.domain;

import java.io.Serializable;

public class SalukiHost implements Serializable {

    private static final long serialVersionUID = 1L;
    private String            host;
    private String            port;

    public SalukiHost(String host){
        if (host.contains(":")) {
            String[] hostAndPort = host.split(":");
            this.host = hostAndPort[0];
            this.port = hostAndPort[1];
        } else {
            this.host = host;
        }
    }

    public SalukiHost(String host, String port){
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

}
