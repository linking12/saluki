package com.quancheng.saluki.registry.consul.internal.model;

import java.util.List;

public final class SalukiConsulServiceResp {

    private final List<SalukiConsulService> salukiConsulServices;
    private final Long                      consulIndex;
    private final Boolean                   consulKnownLeader;
    private final Long                      consulLastContact;

    private SalukiConsulServiceResp(Builder builder){
        this.salukiConsulServices = builder.salukiConsulServices;
        this.consulIndex = builder.consulIndex;
        this.consulKnownLeader = builder.consulKnownLeader;
        this.consulLastContact = builder.consulLastContact;
    }

    public List<SalukiConsulService> getSalukiConsulServices() {
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

        private List<SalukiConsulService> salukiConsulServices;
        private Long                      consulIndex;
        private Boolean                   consulKnownLeader;
        private Long                      consulLastContact;

        public Builder withValue(List<SalukiConsulService> value) {
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

        public SalukiConsulServiceResp build() {
            return new SalukiConsulServiceResp(this);
        }

    }
}
