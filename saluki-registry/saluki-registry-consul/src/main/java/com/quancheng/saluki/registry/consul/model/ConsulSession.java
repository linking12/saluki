/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.registry.consul.model;

import com.ecwid.consul.v1.session.model.NewSession;

/**
 * @author shimingliu 2017年1月24日 上午10:46:03
 * @version ConsulSession.java, v 0.0.1 2017年1月24日 上午10:46:03 shimingliu
 */
public final class ConsulSession {

    private NewSession         session;

    private String             sessionId;

    private ConsulEphemralNode ephemralNode;

    public ConsulSession(NewSession session, String sessionId, ConsulEphemralNode ephemralNode){
        super();
        this.session = session;
        this.sessionId = sessionId;
        this.ephemralNode = ephemralNode;
    }

    public NewSession getSession() {
        return session;
    }

    public void setSession(NewSession session) {
        this.session = session;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ConsulEphemralNode getEphemralNode() {
        return ephemralNode;
    }

    public void setEphemralNode(ConsulEphemralNode ephemralNode) {
        this.ephemralNode = ephemralNode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ConsulSession other = (ConsulSession) obj;
        if (sessionId == null) {
            if (other.sessionId != null) return false;
        } else if (!sessionId.equals(other.sessionId)) return false;
        return true;
    }

}
