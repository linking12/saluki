/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.registry.consul.internal;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.health.model.HealthService.Service;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.registry.consul.ConsulConstants;
import com.quancheng.saluki.registry.consul.model.ConsulEphemralNode;
import com.quancheng.saluki.registry.consul.model.ConsulRouterResp;
import com.quancheng.saluki.registry.consul.model.ConsulService;
import com.quancheng.saluki.registry.consul.model.ConsulService2;
import com.quancheng.saluki.registry.consul.model.ConsulServiceResp;
import com.quancheng.saluki.registry.consul.model.ConsulSession;

/**
 * @author shimingliu 2016年12月16日 上午10:32:23
 * @version ConsulClient.java, v 0.0.1 2016年12月16日 上午10:32:23 shimingliu
 */
public class ConsulClient {

    private static final Logger                    log  = LoggerFactory.getLogger(ConsulClient.class);

    private final Object                           lock = new Object();

    private final com.ecwid.consul.v1.ConsulClient client;

    private final TtlScheduler                     ttlScheduler;

    private final ScheduledExecutorService         scheduleRegistry;

    public ConsulClient(String host, int port){
        client = new com.ecwid.consul.v1.ConsulClient(host, port);
        ttlScheduler = new TtlScheduler(client);
        scheduleRegistry = Executors.newScheduledThreadPool(1, new NamedThreadFactory("retryFailedTtl", true));
        scheduleRegistry.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    retryFailedTtl();
                } catch (Throwable e) {
                    log.info("retry registry znode failed", e);
                }
            }
        }, ConsulConstants.HEARTBEAT_CIRCLE, ConsulConstants.HEARTBEAT_CIRCLE, TimeUnit.MILLISECONDS);
        log.info("ConsulEcwidClient init finish. client host:" + host + ", port:" + port);
    }

    private void retryFailedTtl() {
        Set<ConsulService2> failedService = ttlScheduler.getFailedService();
        Set<ConsulSession> failedSession = ttlScheduler.getFailedSession();
        if (failedSession.size() > 0 || failedService.size() > 0) {
            log.debug(String.format("retry to registry failed service %d or failed session %d", failedService.size(),
                                    failedSession.size()));
            for (ConsulService2 consulService2 : failedService) {
                registerService(consulService2.getService());
            }
            Set<Boolean> allSuccess = Sets.newHashSet();
            for (ConsulSession consulSession : failedSession) {
                allSuccess.add(registerEphemralNode(consulSession.getEphemralNode()));
            }
            if (!allSuccess.contains(Boolean.FALSE)) {
                ttlScheduler.cleanFailedTtl();
            }
        }
    }

    public void registerService(ConsulService service) {
        NewService newService = service.getNewService();
        client.agentServiceRegister(newService);
        ConsulService2 consulService2 = new ConsulService2(service, newService);
        ttlScheduler.addHeartbeatServcie(consulService2);
    }

    public void unregisterService(ConsulService service) {
        NewService newService = service.getNewService();
        client.agentServiceDeregister(newService.getId());
        ConsulService2 consulService2 = new ConsulService2(service, newService);
        ttlScheduler.removeHeartbeatServcie(consulService2);
    }

    public Boolean registerEphemralNode(ConsulEphemralNode ephemralNode) {
        String sessionId = null;
        List<Session> sessions = client.getSessionList(QueryParams.DEFAULT).getValue();
        if (sessions != null && !sessions.isEmpty()) {
            for (Session session : sessions) {
                if (session.getName().equals(ephemralNode.getSessionName())) {
                    sessionId = session.getId();
                }
            }
        }
        if (sessionId == null) {
            NewSession newSession = ephemralNode.getNewSession();
            synchronized (lock) {
                sessionId = client.sessionCreate(newSession, QueryParams.DEFAULT).getValue();
            }
        }
        ConsulSession session = new ConsulSession(sessionId, ephemralNode);
        ttlScheduler.addHeartbeatSession(session);
        PutParams kvPutParams = new PutParams();
        kvPutParams.setAcquireSession(sessionId);
        return client.setKVValue(ephemralNode.getEphemralNodeKey(), ephemralNode.getEphemralNodeValue(),
                                 kvPutParams).getValue();
    }

    public ConsulRouterResp lookupRouterMessage(String serviceName, long lastConsulIndex) {
        QueryParams queryParams = new QueryParams(ConsulConstants.CONSUL_BLOCK_TIME_SECONDS, lastConsulIndex);
        Response<GetValue> orgResponse = client.getKVValue(serviceName, queryParams);
        GetValue getValue = orgResponse.getValue();
        if (getValue != null && StringUtils.isNoneBlank(getValue.getValue())) {
            String router = new String(Base64.decodeBase64(getValue.getValue()));
            ConsulRouterResp response = ConsulRouterResp.newResponse()//
                                                        .withValue(router)//
                                                        .withConsulIndex(orgResponse.getConsulIndex())//
                                                        .withConsulLastContact(orgResponse.getConsulLastContact())//
                                                        .withConsulKnowLeader(orgResponse.isConsulKnownLeader())//
                                                        .build();
            return response;
        }
        return null;
    }

    public ConsulServiceResp lookupHealthService(String serviceName, long lastConsulIndex) {
        QueryParams queryParams = new QueryParams(ConsulConstants.CONSUL_BLOCK_TIME_SECONDS, lastConsulIndex);
        Response<List<HealthService>> orgResponse = client.getHealthServices(serviceName, true, queryParams);
        if (orgResponse != null && orgResponse.getValue() != null && !orgResponse.getValue().isEmpty()) {
            List<HealthService> HealthServices = orgResponse.getValue();
            List<ConsulService> ConsulServcies = Lists.newArrayList();
            for (HealthService orgService : HealthServices) {
                Service org = orgService.getService();
                ConsulService newService = ConsulService.newSalukiService()//
                                                        .withAddress(org.getAddress())//
                                                        .withName(org.getService())//
                                                        .withId(org.getId())//
                                                        .withPort(org.getPort().toString())//
                                                        .withTags(org.getTags())//
                                                        .build();
                ConsulServcies.add(newService);
            }
            if (!ConsulServcies.isEmpty()) {
                ConsulServiceResp response = ConsulServiceResp.newResponse()//
                                                              .withValue(ConsulServcies)//
                                                              .withConsulIndex(orgResponse.getConsulIndex())//
                                                              .withConsulLastContact(orgResponse.getConsulLastContact())//
                                                              .withConsulKnowLeader(orgResponse.isConsulKnownLeader())//
                                                              .build();
                return response;
            }
        }
        return null;
    }

}
