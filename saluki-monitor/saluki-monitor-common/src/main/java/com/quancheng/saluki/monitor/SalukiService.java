package com.quancheng.saluki.monitor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

public class SalukiService implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String      serviceName;

    private Set<SalukiHost>   provideHost;

    private Set<SalukiHost>   consumerHost;

    public SalukiService(String serviceName){
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Set<SalukiHost> getProvideHost() {
        return provideHost;
    }

    public void setProvideHost(Set<SalukiHost> provideHost) {
        this.provideHost = provideHost;
    }

    public void addProviderHosts(Collection<SalukiHost> provideHost) {
        if (this.provideHost == null) {
            this.provideHost = Sets.newConcurrentHashSet();
        }
        this.provideHost.addAll(provideHost);
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
        return true;
    }

    @Override
    public String toString() {
        return "SalukiService [serviceName=" + serviceName + ", prividerHost=" + provideHost + ", consumerHost="
               + consumerHost + "]";
    }

}
