package com.quancheng.saluki.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

public class Application implements Serializable {

    private static final long  serialVersionUID = 1L;

    private final String       appName;

    private Set<GrpcService> services;

    public Application(String appName){
        this.appName = appName;
    }

    public Set<GrpcService> getServices() {
        return services;
    }

    public void setServices(Set<GrpcService> services) {
        this.services = services;
    }

    public String getAppName() {
        return appName;
    }

    public void addService(GrpcService service) {
        if (this.services == null) {
            this.services = Sets.newConcurrentHashSet();
        }
        GrpcService machService = null;
        for (Iterator<GrpcService> it = this.services.iterator(); it.hasNext();) {
            GrpcService targetService = it.next();
            Boolean isServiceNameMatch = service.getServiceName().equals(targetService.getServiceName());
            if (isServiceNameMatch) {
                machService = targetService;
            }
        }
        if (machService != null) {
            if (service.getConsumerHost() != null) {
                machService.addConsumerHosts(service.getConsumerHost());
            }
            if (service.getProviderHost() != null) {
                machService.addProviderHosts(service.getProviderHost());
            }
            this.services.add(machService);
        } else {
            this.services.add(service);
        }
    }

    public void addServices(Collection<GrpcService> services) {
        for (Iterator<GrpcService> it = services.iterator(); it.hasNext();) {
            addService(it.next());
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appName == null) ? 0 : appName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Application other = (Application) obj;
        if (appName == null) {
            if (other.appName != null) return false;
        } else if (!appName.equals(other.appName)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SalukiApplication [appName=" + appName + ", services=" + services + "]";
    }

}
