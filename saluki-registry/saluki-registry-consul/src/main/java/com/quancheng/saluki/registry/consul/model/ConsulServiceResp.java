package com.quancheng.saluki.registry.consul.model;

import java.util.List;

public final class ConsulServiceResp {

    private final List<ConsulService> salukiConsulServices;
    private final Long                consulIndex;
    private final Boolean             consulKnownLeader;
    private final Long                consulLastContact;

    private ConsulServiceResp(Builder builder){
        this.salukiConsulServices = builder.salukiConsulServices;
        this.consulIndex = builder.consulIndex;
        this.consulKnownLeader = builder.consulKnownLeader;
        this.consulLastContact = builder.consulLastContact;
    }

    public List<ConsulService> getSalukiConsulServices() {
        return salukiConsulServices;
    }

    public Long getConsulIndex() {
        return consulIndex;
    }

    public Boolean getConsulKnownLeader() {
        return consulKnownLeader;
    }

    public Long getConsulLastContact() {
        return consulLastContact;
    }

    public static Builder newResponse() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder {

        private List<ConsulService> salukiConsulServices;
        private Long                consulIndex;
        private Boolean             consulKnownLeader;
        private Long                consulLastContact;

        public Builder withValue(List<ConsulService> value) {
            this.salukiConsulServices = value;
            return this;
        }

        public Builder withConsulIndex(Long consulIndex) {
            this.consulIndex = consulIndex;
            return this;
        }

        public Builder withConsulKnowLeader(Boolean consulKnownLeader) {
            this.consulKnownLeader = consulKnownLeader;
            return this;
        }

        public Builder withConsulLastContact(Long consulLastContact) {
            this.consulLastContact = consulLastContact;
            return this;
        }

        public ConsulServiceResp build() {
            return new ConsulServiceResp(this);
        }

    }
}
