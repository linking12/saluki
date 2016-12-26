package com.quancheng.saluki.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

public class GrpcService implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String      application;

    private final String      version;

    private final String      serviceName;

    private Set<GrpcHost>   providerHost;

    private Set<GrpcHost>   consumerHost;

    public GrpcService(String application, String version, String serviceName){
        this.application = application;
        this.version = version;
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApplication() {
        return application;
    }

    public Set<GrpcHost> getProviderHost() {
        return providerHost;
    }

    public void setProviderHost(Set<GrpcHost> providerHost) {
        this.providerHost = providerHost;
    }

    public void addProviderHosts(Collection<GrpcHost> provideHost) {
        if (this.providerHost == null) {
            this.providerHost = Sets.newConcurrentHashSet();
        }
        this.providerHost.addAll(provideHost);
    }

    public Set<GrpcHost> getConsumerHost() {
        return consumerHost;
    }

    public void setConsumerHost(Set<GrpcHost> consumerHost) {
        this.consumerHost = consumerHost;
    }

    public void addConsumerHosts(Set<GrpcHost> consumerHosts) {
        if (this.consumerHost == null) {
            this.consumerHost = Sets.newConcurrentHashSet();
        }
        this.consumerHost.addAll(consumerHosts);
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((application == null) ? 0 : application.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        GrpcService other = (GrpcService) obj;
        if (application == null) {
            if (other.application != null) return false;
        } else if (!application.equals(other.application)) return false;
        if (serviceName == null) {
            if (other.serviceName != null) return false;
        } else if (!serviceName.equals(other.serviceName)) return false;
        if (version == null) {
            if (other.version != null) return false;
        } else if (!version.equals(other.version)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SalukiService [serviceName=" + serviceName + ", prividerHost=" + providerHost + ", consumerHost="
               + consumerHost + "]";
    }

}
