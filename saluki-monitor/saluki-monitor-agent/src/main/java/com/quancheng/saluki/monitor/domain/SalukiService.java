package com.quancheng.saluki.monitor.domain;

import java.io.Serializable;
import java.util.List;

public class SalukiService implements Serializable {

    private static final long   serialVersionUID = 1L;

    private String              name;

    private String              applicationName;

    private SalukiService       parent;

    private List<SalukiService> children;

    private List<SalukiHost>    consumerHost;

    private List<SalukiHost>    providerHost;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public SalukiService getParent() {
        return parent;
    }

    public void setParent(SalukiService parent) {
        this.parent = parent;
    }

    public List<SalukiService> getChildren() {
        return children;
    }

    public void setChildren(List<SalukiService> children) {
        this.children = children;
    }

    public List<SalukiHost> getConsumerHost() {
        return consumerHost;
    }

    public void setConsumerHost(List<SalukiHost> consumerHost) {
        this.consumerHost = consumerHost;
    }

    public List<SalukiHost> getProviderHost() {
        return providerHost;
    }

    public void setProviderHost(List<SalukiHost> providerHost) {
        this.providerHost = providerHost;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SalukiService other = (SalukiService) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SalukiService [name=" + name + ", applicationName=" + applicationName + ", parent=" + parent
               + ", children=" + children + ", consumerHost=" + consumerHost + ", providerHost=" + providerHost + "]";
    }

}
