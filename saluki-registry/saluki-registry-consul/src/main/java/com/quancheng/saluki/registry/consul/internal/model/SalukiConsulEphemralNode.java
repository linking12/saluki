package com.quancheng.saluki.registry.consul.internal.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import com.google.gson.Gson;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.registry.consul.ConsulRegistry;

public final class SalukiConsulEphemralNode {

    private final static Gson gson = new Gson();

    private final String      host;

    private final String      serverInfo;

    private final String      group;

    private final String      serviceName;

    private final String      interval;

    private final String      flag;

    private final String      rpcPort;

    private final String      httpServerPort;

    private SalukiConsulEphemralNode(Builder builder){
        String serverInfo = System.getProperty(SalukiConstants.REGISTRY_SERVER_PARAM);
        this.serverInfo = serverInfo;
        this.group = builder.group;
        this.serviceName = builder.serviceName;
        this.interval = builder.interval;
        this.flag = builder.flag;
        this.rpcPort = builder.rpcPort;
        @SuppressWarnings("unchecked")
        Map<String, String> serverParam = gson.fromJson(serverInfo, Map.class);
        String serverHost = serverParam.get("serverHost");
        this.httpServerPort = serverParam.get("serverHttpPort");
        // 如果是docker，ip会变化，需要手动注入下
        if (serverHost != null) {
            this.host = serverHost;
        } else {
            this.host = builder.host;
        }
    }

    public NewSession getNewSession() {
        NewSession newSersson = new NewSession();
        newSersson.setName(getSessionName());
        newSersson.setLockDelay(0);
        newSersson.setBehavior(Session.Behavior.DELETE);
        newSersson.setTtl(this.interval + "s");
        return newSersson;
    }

    public String getSessionName() {
        String key;
        if (this.flag.equals("provider")) {
            key = ConsulRegistry.CONSUL_SERVICE_PRE + this.group + "_" + this.serviceName + "_provider" + "_"
                  + this.host + "_" + this.rpcPort;
        } else {
            key = ConsulRegistry.CONSUL_SERVICE_PRE + this.group + "_" + this.serviceName + "_consumer" + "_"
                  + this.host + "_" + this.httpServerPort;
        }
        try {
            return URLEncoder.encode(key, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 执行到这里是有问题的
        return UUID.randomUUID().toString();
    }

    public String getKey() {
        String key;
        if (this.flag.equals("provider")) {
            key = ConsulRegistry.CONSUL_SERVICE_PRE + this.group + "/" + this.serviceName + "/provider" + "/"
                  + this.host + ":" + this.rpcPort;
        } else {
            key = ConsulRegistry.CONSUL_SERVICE_PRE + this.group + "/" + this.serviceName + "/consumer" + "/"
                  + this.host + ":" + this.httpServerPort;
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

        private String host;

        private String rpcPort;

        private String group;

        private String serviceName;

        private String interval;

        private String flag;

        public Builder withFlag(String flag) {
            this.flag = substituteEnvironmentVariables(flag);
            return this;
        }

        public Builder withHost(String host) {
            this.host = substituteEnvironmentVariables(host);
            return this;
        }

        public Builder withRpcPort(String rpcPort) {
            this.rpcPort = substituteEnvironmentVariables(rpcPort);
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
