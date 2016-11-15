package com.quancheng.saluki.registry.consul.internal.model;

import java.util.Map;

import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.registry.consul.ConsulRegistry;

public final class SalukiConsulEphemralNode {

    private final String host;

    private final String serverInfo;

    private final String group;

    private final String serviceName;

    private final String interval;

    private final String flag;

    private SalukiConsulEphemralNode(Builder builder){
        this.serverInfo = builder.serverInfo;
        this.group = builder.group;
        this.serviceName = builder.serviceName;
        this.interval = builder.interval;
        this.flag = builder.flag;
        this.host = builder.host;
    }

    public NewSession getNewSession() {
        NewSession newSersson = new NewSession();
        newSersson.setName(this.serverInfo);
        newSersson.setLockDelay(0);
        newSersson.setBehavior(Session.Behavior.DELETE);
        newSersson.setTtl(this.interval + "s");
        return newSersson;
    }

    public String getKey() {
        String key;
        if (this.flag.equals("provider")) {
            key = ConsulRegistry.CONSUL_SERVICE_PRE + this.group + "/" + this.serviceName + "/provider" + "/"
                  + this.host;
        } else {
            key = ConsulRegistry.CONSUL_SERVICE_PRE + this.group + "/" + this.serviceName + "/consumer" + "/"
                  + this.host;
        }
        return key;
    }

    public String getServerInfo() {
        return serverInfo;
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
        return "SalukiConsulEphemralNode [consumerinfo=" + serverInfo + ", group=" + group + ", serviceName="
               + serviceName + ", interval=" + interval + "]";
    }

    public static Builder newEphemralNode() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder {

        private final static Gson gson = new Gson();

        private String            host;

        private String            serverInfo;

        private String            group;

        private String            serviceName;

        private String            interval;

        private String            flag;

        public Builder withFlag(String flag) {
            this.flag = substituteEnvironmentVariables(flag);
            return this;
        }

        public Builder withHost(String host) {
            this.host = substituteEnvironmentVariables(host);
            String serverInfo = System.getProperty(SalukiConstants.REGISTRY_CLIENT_PARAM);
            this.serverInfo = serverInfo;
            Map<String, String> clientParam = gson.fromJson(serverInfo, new TypeToken<Map<String, String>>() {
            }.getType());
            String serverHost = clientParam.get("serverHost");
            if (serverHost != null) {
                this.host = serverHost;
            }
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
            if (flag == null) {
                throw new java.lang.IllegalArgumentException("Required flag is missing");
            }
            if (serverInfo == null) {
                throw new java.lang.IllegalArgumentException("Required serverInfo is missing");
            }
            if (group == null) {
                throw new java.lang.IllegalArgumentException("Required group is missing for EphemralNode ");
            }
            if (serviceName == null) {
                throw new java.lang.IllegalArgumentException("Required servicename is missing for EphemralNode ");
            }
            if (interval == null) {
                throw new java.lang.IllegalArgumentException("Required interval is missing for EphemralNode ");
            }
            return new SalukiConsulEphemralNode(this);
        }

    }

}
