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
import com.quancheng.saluki.registry.consul.model.ConsulServiceResp;

/**
 * @author shimingliu 2016年12月16日 上午10:32:23
 * @version ConsulClient.java, v 0.0.1 2016年12月16日 上午10:32:23 shimingliu
 */
public class ConsulClient {

    private static final Logger                    log    = LoggerFactory.getLogger(ConsulClient.class);

    private final Object                           lock   = new Object();

    private final com.ecwid.consul.v1.ConsulClient client;

    private final TtlScheduler                     ttlScheduler;

    private final ScheduledExecutorService         scheduleRegistryZnode;

    private final Set<ConsulEphemralNode>          znodes = Sets.newConcurrentHashSet();

    public ConsulClient(String host, int port){
        client = new com.ecwid.consul.v1.ConsulClient(host, port);
        ttlScheduler = new TtlScheduler(client);
        scheduleRegistryZnode = Executors.newScheduledThreadPool(1,
                                                                 new NamedThreadFactory("RegisterEphemralNode", true));
        scheduleRegistryZnode.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                retryRegisterEphemralNode();
            }
        }, 0, ConsulConstants.TTL * 5, TimeUnit.SECONDS);
        log.info("ConsulEcwidClient init finish. client host:" + host + ", port:" + port);
    }

    private void retryRegisterEphemralNode() {
        for (ConsulEphemralNode znode : znodes) {
            registerEphemralNode(znode);
        }
    }

    public void registerService(ConsulService service) {
        NewService newService = service.getNewService();
        client.agentServiceRegister(newService);
        ttlScheduler.addHeartbeatServcie(newService);
    }

    public void unregisterService(String serviceid) {
        client.agentServiceDeregister(serviceid);
        ttlScheduler.removeHeartbeatServcie(serviceid);
    }

    public void registerEphemralNode(ConsulEphemralNode ephemralNode) {
        String sessionId = null;
        List<Session> sessions = client.getSessionList(QueryParams.DEFAULT).getValue();
        if (sessions != null && !sessions.isEmpty()) {
            for (Session session : sessions) {
                if (session.getName().equals(ephemralNode.getSessionName())) {
                    sessionId = session.getId();
                    ttlScheduler.addHeartbeatSession(sessionId);
                }
            }
        }
        if (sessionId == null) {
            NewSession newSession = ephemralNode.getNewSession();
            synchronized (lock) {
                sessionId = client.sessionCreate(newSession, QueryParams.DEFAULT).getValue();
                ttlScheduler.addHeartbeatSession(sessionId);
            }
        }
        PutParams kvPutParams = new PutParams();
        kvPutParams.setAcquireSession(sessionId);
        client.setKVValue(ephemralNode.getEphemralNodeKey(), ephemralNode.getEphemralNodeValue(), kvPutParams);
        znodes.add(ephemralNode);
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
