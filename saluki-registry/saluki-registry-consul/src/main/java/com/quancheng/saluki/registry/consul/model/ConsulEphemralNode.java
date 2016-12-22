package com.quancheng.saluki.registry.consul.model;

import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.registry.consul.GrpcURLUtils;

public final class ConsulEphemralNode {

    private final GrpcURL        url;

    private final String         interval;

    private final ThrallRoleType ephemralType;

    private ConsulEphemralNode(Builder builder){
        this.url = builder.url;
        this.interval = builder.interval;
        this.ephemralType = builder.ephemralType;
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
        return ephemralType.name() + "_" + url.getHost() + "_" + url.getPort();
    }

    public String getEphemralNodeKey() {
        return GrpcURLUtils.ephemralNodePath(url, ephemralType);
    }

    public String getEphemralNodeValue() {
        return url.toFullString();
    }

    public static Builder newEphemralNode() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder {

        private GrpcURL        url;

        private String         interval;

        private ThrallRoleType ephemralType;

        public Builder withUrl(GrpcURL url) {
            this.url = url;
            return this;
        }

        public Builder withEphemralType(ThrallRoleType ephemralType) {
            this.ephemralType = ephemralType;
            return this;
        }

        public Builder withCheckInterval(String interval) {
            this.interval = substituteEnvironmentVariables(interval);
            return this;
        }

        public ConsulEphemralNode build() {
            return new ConsulEphemralNode(this);
        }

    }

}
