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
import com.quancheng.saluki.core.utils.NamedThreadFactory;

public class TtlScheduler {

    private static final Logger                   log = LoggerFactory.getLogger(TtlScheduler.class);
    private final Map<String, ScheduledFuture<?>> serviceHeartbeatFutures;
    private final ScheduledExecutorService        heartbeatServiceExecutor;
    private final ScheduledExecutorService        heartbeatSessionExecutor;
    private final ConsulClient                    client;

    public TtlScheduler(ConsulClient client){
        this.client = client;
        this.serviceHeartbeatFutures = Maps.newConcurrentMap();
        this.heartbeatServiceExecutor = Executors.newScheduledThreadPool(1,
                                                                         new NamedThreadFactory("SalukiCheckServiceTimer",
                                                                                                true));
        this.heartbeatSessionExecutor = Executors.newScheduledThreadPool(1,
                                                                         new NamedThreadFactory("SalukiCheckSessionTimer",
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
        heartbeatSessionExecutor.scheduleAtFixedRate(new ConsulHeartbeatSessionTask(sessionId),
                                                     ConsulConstants.HEARTBEAT_CIRCLE, ConsulConstants.HEARTBEAT_CIRCLE,
                                                     TimeUnit.MILLISECONDS);
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
