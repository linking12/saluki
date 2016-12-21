/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.registry.consul.internal;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.session.model.Session;
import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.registry.consul.ConsulConstants;

/**
 * @author shimingliu 2016年12月16日 上午10:32:37
 * @version TtlScheduler.java, v 0.0.1 2016年12月16日 上午10:32:37 shimingliu
 */
public class TtlScheduler {

    private static final Logger                   log                     = LoggerFactory.getLogger(TtlScheduler.class);
    private final Map<String, ScheduledFuture<?>> serviceHeartbeatFutures = Maps.newConcurrentMap();
    private final Map<String, ScheduledFuture<?>> nodeHeartbeatFutures    = Maps.newConcurrentMap();
    private final ScheduledExecutorService        heartbeatServiceExecutor;
    private final ScheduledExecutorService        heartbeatSessionExecutor;
    private final ConsulClient                    client;

    public TtlScheduler(ConsulClient client){
        this.client = client;
        this.heartbeatServiceExecutor = Executors.newScheduledThreadPool(1,
                                                                         new NamedThreadFactory("ThrallCheckServiceTimer",
                                                                                                true));
        this.heartbeatSessionExecutor = Executors.newScheduledThreadPool(1,
                                                                         new NamedThreadFactory("ThrallCheckSessionTimer",
                                                                                                true));
    }

    public void addHeartbeatServcie(final NewService service) {
        ScheduledFuture<?> future = heartbeatServiceExecutor.scheduleAtFixedRate(new ConsulHeartbeatServiceTask(service.getId()),
                                                                                 ConsulConstants.HEARTBEAT_CIRCLE,
                                                                                 ConsulConstants.HEARTBEAT_CIRCLE,
                                                                                 TimeUnit.MILLISECONDS);
        serviceHeartbeatFutures.put(service.getId(), future);
    }

    public void addHeartbeatSession(final String sessionId) {
        if (!nodeHeartbeatFutures.containsKey(sessionId)) {
            ScheduledFuture<?> future = heartbeatSessionExecutor.scheduleAtFixedRate(new ConsulHeartbeatSessionTask(sessionId),
                                                                                     ConsulConstants.HEARTBEAT_CIRCLE,
                                                                                     ConsulConstants.HEARTBEAT_CIRCLE,
                                                                                     TimeUnit.MILLISECONDS);
            nodeHeartbeatFutures.put(sessionId, future);
        }
    }

    public void removeHeartbeatServcie(final String serviceId) {
        Future<?> task = serviceHeartbeatFutures.get(serviceId);
        if (task != null) {
            task.cancel(true);
        }
        serviceHeartbeatFutures.remove(serviceId);
    }

    private class ConsulHeartbeatServiceTask implements Runnable {

        private String checkId;

        ConsulHeartbeatServiceTask(String serviceId){
            this.checkId = serviceId;
            if (!checkId.startsWith("service:")) {
                checkId = "service:" + checkId;
            }
        }

        @Override
        public void run() {
            client.agentCheckPass(checkId);
            log.debug("Sending consul heartbeat for: " + checkId);
        }
    }

    private class ConsulHeartbeatSessionTask implements Runnable {

        private String sessionId;

        ConsulHeartbeatSessionTask(String sessionId){
            this.sessionId = sessionId;
        }

        @Override
        public void run() {
            Response<Session> response = client.renewSession(sessionId, QueryParams.DEFAULT);
            log.debug("Sending consul heartbeat for: " + response.getValue().getId());
        }
    }
}
