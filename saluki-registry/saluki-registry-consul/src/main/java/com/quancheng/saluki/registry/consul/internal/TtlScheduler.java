/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.registry.consul.internal;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.google.common.collect.Sets;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.registry.consul.ConsulConstants;
import com.quancheng.saluki.registry.consul.model.ConsulService2;
import com.quancheng.saluki.registry.consul.model.ConsulSession;

/**
 * @author shimingliu 2016年12月16日 上午10:32:37
 * @version TtlScheduler.java, v 0.0.1 2016年12月16日 上午10:32:37 shimingliu
 */
public class TtlScheduler {

    private static final Logger            log                      = LoggerFactory.getLogger(TtlScheduler.class);

    private final Set<ConsulService2>      services                 = Sets.newConcurrentHashSet();

    private final Set<ConsulSession>       sessions                 = Sets.newConcurrentHashSet();

    private final Set<ConsulService2>      failedservices           = Sets.newConcurrentHashSet();

    private final Set<ConsulSession>       failedsessions           = Sets.newConcurrentHashSet();

    private final ScheduledExecutorService heartbeatServiceExecutor = Executors.newScheduledThreadPool(1,
                                                                                                       new NamedThreadFactory("CheckServiceTimer",
                                                                                                                              true));

    private final ScheduledExecutorService heartbeatSessionExecutor = Executors.newScheduledThreadPool(1,
                                                                                                       new NamedThreadFactory("CheckSessionTimer",
                                                                                                                              true));

    private final ConsulClient             client;

    public TtlScheduler(ConsulClient client){
        this.client = client;
        heartbeatServiceExecutor.scheduleAtFixedRate(new ConsulHeartbeatServiceTask(), ConsulConstants.HEARTBEAT_CIRCLE,
                                                     ConsulConstants.HEARTBEAT_CIRCLE, TimeUnit.MILLISECONDS);
        heartbeatSessionExecutor.scheduleAtFixedRate(new ConsulHeartbeatSessionTask(), ConsulConstants.HEARTBEAT_CIRCLE,
                                                     ConsulConstants.HEARTBEAT_CIRCLE, TimeUnit.MILLISECONDS);
    }

    public void addHeartbeatServcie(final ConsulService2 service) {
        services.add(service);
    }

    public void addHeartbeatSession(final ConsulSession session) {
        sessions.add(session);
    }

    public void removeHeartbeatServcie(final ConsulService2 service) {
        services.remove(service);
    }

    public Set<ConsulService2> getFailedService() {
        return failedservices;
    }

    public Set<ConsulSession> getFailedSession() {
        return failedsessions;
    }

    public void cleanFailedTtl() {
        failedsessions.clear();
        failedservices.clear();
    }

    private class ConsulHeartbeatServiceTask implements Runnable {

        @Override
        public void run() {
            for (ConsulService2 service : services) {
                try {
                    String checkId = service.getNewService().getId();
                    if (!checkId.startsWith("service:")) {
                        checkId = "service:" + checkId;
                    }
                    client.agentCheckPass(checkId);
                    log.debug("Sending consul heartbeat for: " + checkId);
                } catch (Throwable e) {
                    failedservices.add(service);
                    services.remove(service);
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private class ConsulHeartbeatSessionTask implements Runnable {

        @Override
        public void run() {
            Set<String> sessionIds = Sets.newHashSet();
            for (ConsulSession session : sessions) {
                try {
                    String sessionId = session.getSessionId();
                    if (!sessionIds.contains(sessionId)) {
                        client.renewSession(sessionId, QueryParams.DEFAULT);
                        sessionIds.add(sessionId);
                    }
                    log.debug("Sending consul heartbeat for: " + sessionId);
                } catch (Throwable e) {
                    failedsessions.addAll(sessions);
                    sessions.clear();
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
