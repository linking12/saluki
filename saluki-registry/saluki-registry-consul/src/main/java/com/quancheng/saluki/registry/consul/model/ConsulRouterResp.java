/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.registry.consul.model;

/**
 * @author shimingliu 2016年12月29日 下午4:10:13
 * @version ConsulRouterResp.java, v 0.0.1 2016年12月29日 下午4:10:13 shimingliu
 */
public final class ConsulRouterResp {

    private final String  salukiConsulRouter;
    private final Long    consulIndex;
    private final Boolean consulKnownLeader;
    private final Long    consulLastContact;

    private ConsulRouterResp(Builder builder){
        this.salukiConsulRouter = builder.salukiConsulRouter;
        this.consulIndex = builder.consulIndex;
        this.consulKnownLeader = builder.consulKnownLeader;
        this.consulLastContact = builder.consulLastContact;
    }

    public String getSalukiConsulRouter() {
        return salukiConsulRouter;
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

        private String  salukiConsulRouter;
        private Long    consulIndex;
        private Boolean consulKnownLeader;
        private Long    consulLastContact;

        public Builder withValue(String routerMessage) {
            this.salukiConsulRouter = routerMessage;
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

        public ConsulRouterResp build() {
            return new ConsulRouterResp(this);
        }

    }

}
