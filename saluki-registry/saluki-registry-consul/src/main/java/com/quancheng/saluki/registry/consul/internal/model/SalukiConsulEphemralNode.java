package com.quancheng.saluki.registry.consul.internal.model;

import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import com.quancheng.saluki.registry.consul.ConsulRegistry;

public final class SalukiConsulEphemralNode {

    private final String ip;

    private final String group;

    private final String serviceName;

    private final String interval;

    private SalukiConsulEphemralNode(Builder builder){
        this.ip = builder.ip;
        this.group = builder.group;
        this.serviceName = builder.serviceName;
        this.interval = builder.interval;
    }

    public NewSession getNewSession() {
        NewSession newSersson = new NewSession();
        newSersson.setName(this.ip);
        newSersson.setBehavior(Session.Behavior.DELETE);
        newSersson.setTtl(this.interval + "s");
        return newSersson;
    }

    public String getKey() {
        return ConsulRegistry.CONSUL_SERVICE_PRE + this.group + "/" + this.serviceName + "/" + this.ip;
    }

    public String getValue() {
        return this.ip;
    }

    public String getIp() {
        return ip;
    }

    public String getGroup() {
        return group;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getInterval() {
        return interval;
    }

    @Override
    public String toString() {
        return "SalukiConsulEphemralNode [ip=" + ip + ", group=" + group + ", serviceName=" + serviceName
               + ", interval=" + interval + "]";
    }

    public static Builder newEphemralNode() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder {

        private String ip;

        private String group;

        private String serviceName;

        private String interval;

        public Builder withIp(String ip) {
            this.ip = substituteEnvironmentVariables(ip);
            return this;
        }

        public Builder withGroup(String group) {
            this.group = substituteEnvironmentVariables(group);
            return this;
        }

        public Builder withServiceName(String serviceName) {
            this.serviceName = substituteEnvironmentVariables(serviceName);
            return this;
        }

        public Builder withCheckInterval(String interval) {
            this.interval = substituteEnvironmentVariables(interval);
            return this;
        }

        public SalukiConsulEphemralNode build() {
            if (ip == null) {
                throw new java.lang.IllegalArgumentException("Required client ip is missing");
            }
            if (group == null) {
                throw new java.lang.IllegalArgumentException("Required client group is missing for EphemralNode ");
            }
            if (serviceName == null) {
                throw new java.lang.IllegalArgumentException("Required client servicename is missing for EphemralNode ");
            }
            if (interval == null) {
                throw new java.lang.IllegalArgumentException("Required interval is missing for EphemralNode ");
            }
            return new SalukiConsulEphemralNode(this);
        }

    }

}
