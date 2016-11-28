package com.quancheng.saluki.core.grpc.monitor;

import java.io.Serializable;

import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.monitor.MonitorService;

public class Statistics implements Serializable {

    private static final long serialVersionUID = 8843156162686978653L;

    private SalukiURL         url;

    private String            application;

    private String            service;

    private String            method;

    private String            group;

    private String            version;

    private String            client;

    private String            server;

    public Statistics(SalukiURL url){
        this.url = url;
        this.application = url.getParameter(MonitorService.APPLICATION);
        this.service = url.getParameter(MonitorService.INTERFACE);
        this.method = url.getParameter(MonitorService.METHOD);
        this.group = url.getParameter(MonitorService.GROUP);
        this.version = url.getParameter(MonitorService.VERSION);
        this.client = url.getParameter(MonitorService.CONSUMER, url.getAddress());
        this.server = url.getParameter(MonitorService.PROVIDER, url.getAddress());
    }

    public SalukiURL getUrl() {
        return url;
    }

    public void setUrl(SalukiURL url) {
        this.url = url;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Statistics other = (Statistics) obj;
        if (group == null) {
            if (other.group != null) return false;
        } else if (!group.equals(other.group)) return false;
        if (method == null) {
            if (other.method != null) return false;
        } else if (!method.equals(other.method)) return false;
        if (service == null) {
            if (other.service != null) return false;
        } else if (!service.equals(other.service)) return false;
        return true;
    }

    @Override
    public String toString() {
        return url.toString();
    }

}
