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
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.session.model.Session;
import com.google.common.collect.Sets;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.registry.consul.ConsulConstants;

/**
 * @author shimingliu 2016年12月16日 上午10:32:37
 * @version TtlScheduler.java, v 0.0.1 2016年12月16日 上午10:32:37 shimingliu
 */
public class TtlScheduler {

    private static final Logger            log                      = LoggerFactory.getLogger(TtlScheduler.class);

    private final Set<String>              serviceIds               = Sets.newConcurrentHashSet();

    private final Set<String>              sessionIds               = Sets.newConcurrentHashSet();

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

    public void addHeartbeatServcie(final NewService service) {
        serviceIds.add(service.getId());
    }

    public void addHeartbeatSession(final String sessionId) {
        sessionIds.add(sessionId);
    }

    public void removeHeartbeatServcie(final String serviceId) {
        serviceIds.remove(serviceId);
    }

    private class ConsulHeartbeatServiceTask implements Runnable {

        @Override
        public void run() {
            for (String checkId : serviceIds) {
                if (!checkId.startsWith("service:")) {
                    checkId = "service:" + checkId;
                }
                client.agentCheckPass(checkId);
                log.debug("Sending consul heartbeat for: " + checkId);
            }
        }
    }

    private class ConsulHeartbeatSessionTask implements Runnable {

        @Override
        public void run() {
            for (String sessionId : sessionIds) {
                Response<Session> response = client.renewSession(sessionId, QueryParams.DEFAULT);
                log.debug("Sending consul heartbeat for: " + response.getValue().getId());
            }
        }
    }
}
