package com.quancheng.saluki.monitor.domain;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.Sets;

public class SalukiService implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String      serviceName;

    private String            status;

    private Set<SalukiHost>   prividerHost;

    private Set<SalukiHost>   consumerHost;

    public SalukiService(String serviceName){
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<SalukiHost> getPrividerHost() {
        return prividerHost;
    }

    public void setPrividerHost(Set<SalukiHost> prividerHost) {
        this.prividerHost = prividerHost;
    }

    public void addPrividerHost(Set<SalukiHost> prividerHosts) {
        if (this.prividerHost == null) {
            this.prividerHost = Sets.newConcurrentHashSet();
        }
        this.prividerHost.addAll(prividerHosts);
    }

    public Set<SalukiHost> getConsumerHost() {
        return consumerHost;
    }

    public void setConsumerHost(Set<SalukiHost> consumerHost) {
        this.consumerHost = consumerHost;
    }

    public void addConsumerHosts(Set<SalukiHost> consumerHosts) {
        if (this.consumerHost == null) {
            this.consumerHost = Sets.newConcurrentHashSet();
        }
        this.consumerHost.addAll(consumerHosts);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SalukiService other = (SalukiService) obj;
        if (serviceName == null) {
            if (other.serviceName != null) return false;
        } else if (!serviceName.equals(other.serviceName)) return false;
        if (status == null) {
            if (other.status != null) return false;
        } else if (!status.equals(other.status)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SalukiService [serviceName=" + serviceName + ", status=" + status + ", prividerHost=" + prividerHost
               + ", consumerHost=" + consumerHost + "]";
    }

}
