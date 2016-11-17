package com.quancheng.saluki.monitor.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

public class SalukiApplication implements Serializable {

    private static final long  serialVersionUID = 1L;

    private final String       appName;

    private Set<SalukiService> services;

    public SalukiApplication(String appName){
        this.appName = appName;
    }

    public Set<SalukiService> getServices() {
        return services;
    }

    public void setServices(Set<SalukiService> services) {
        this.services = services;
    }

    public String getAppName() {
        return appName;
    }

    public void addService(SalukiService service) {
        if (this.services == null) {
            this.services = Sets.newConcurrentHashSet();
        }
        SalukiService machService = null;
        for (Iterator<SalukiService> it = this.services.iterator(); it.hasNext();) {
            SalukiService targetService = it.next();
            Boolean isServiceNameMatch = service.getServiceName().equals(targetService.getServiceName());
            Boolean isStatusMatch = service.getServiceName().equals(targetService.getStatus());
            if (isServiceNameMatch && isStatusMatch) {
                machService = targetService;
            }
        }
        if (machService != null) {
            this.services.remove(machService);
            if (service.getConsumerHost() != null) {
                machService.addConsumerHosts(service.getConsumerHost());
            }
            if (service.getProvideHost() != null) {
                machService.addProviderHosts(service.getProvideHost());
            }
            this.services.add(machService);
        } else {
            this.services.add(service);
        }
    }

    public void addServices(Collection<SalukiService> services) {
        for (Iterator<SalukiService> it = services.iterator(); it.hasNext();) {
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
        SalukiApplication other = (SalukiApplication) obj;
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
