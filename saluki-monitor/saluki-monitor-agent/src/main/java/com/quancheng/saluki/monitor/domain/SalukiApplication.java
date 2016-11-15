package com.quancheng.saluki.monitor.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SalukiApplication implements Serializable {

    private static final long      serialVersionUID = 1L;

    private String                 name;

    private SalukiApplication      parent;

    private Set<SalukiApplication> children;

    private Set<SalukiHost>        hosts;

    public SalukiApplication(String applicationName){
        this.name = applicationName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SalukiApplication getParent() {
        return parent;
    }

    public void setParent(SalukiApplication parent) {
        this.parent = parent;
    }

    public Set<SalukiApplication> getChildren() {
        return children;
    }

    public void setChildren(Set<SalukiApplication> children) {
        this.children = children;
    }

    public synchronized void addChild(SalukiApplication childApplication) {
        if (this.children == null) {
            this.children = new HashSet<SalukiApplication>();
        }
        this.children.add(childApplication);
    }

    public synchronized void addAllChild(Collection<SalukiApplication> childApplications) {
        if (this.children == null) {
            this.children = new HashSet<SalukiApplication>();
        }
        this.children.addAll(childApplications);
    }

    public Set<SalukiHost> getHosts() {
        return hosts;
    }

    public void setHosts(Set<SalukiHost> hosts) {
        this.hosts = hosts;
    }

    public synchronized void addHost(SalukiHost host) {
        if (this.hosts == null) {
            this.hosts = new HashSet<SalukiHost>();
        }
        this.hosts.add(host);
    }

    public synchronized void addAllHost(Collection<SalukiHost> hosts) {
        if (this.hosts == null) {
            this.hosts = new HashSet<SalukiHost>();
        }
        this.hosts.addAll(hosts);
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
        SalukiApplication other = (SalukiApplication) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SalukiApplication [name=" + name + ", parent=" + parent + ", children=" + children + ", hosts=" + hosts
               + "]";
    }

}
