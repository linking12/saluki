package com.quancheng.saluki.monitor.domain;

import java.io.Serializable;

public class SalukiHost implements Serializable {

    private static final long serialVersionUID = 1L;
    private String            host;
    private String            port;

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
