package com.quancheng.saluki.registry.consul.internal;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.health.model.HealthService.Service;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import com.google.common.collect.Lists;
import com.quancheng.saluki.registry.consul.internal.model.SalukiConsulEphemralNode;
import com.quancheng.saluki.registry.consul.internal.model.SalukiConsulService;
import com.quancheng.saluki.registry.consul.internal.model.SalukiConsulServiceResp;

public class SalukiConsulClient {

    private static final Logger log = LoggerFactory.getLogger(SalukiConsulClient.class);

    private final ConsulClient  client;
    private final TtlScheduler  ttlScheduler;

    public SalukiConsulClient(String host, int port){
        client = new ConsulClient(host, port);
        ttlScheduler = new TtlScheduler(client);
        log.info("ConsulEcwidClient init finish. client host:" + host + ", port:" + port);
    }

    public void registerService(SalukiConsulService service) {
        NewService newService = service.getNewService();
        client.agentServiceRegister(newService);
        ttlScheduler.addHeartbeatServcie(newService);
    }

    public void unregisterService(String serviceid) {
        client.agentServiceDeregister(serviceid);
        ttlScheduler.removeHeartbeatServcie(serviceid);
    }

    public void registerEphemralNode(SalukiConsulEphemralNode ephemralNode) {
        try {
            client.setKVValue(ephemralNode.getKey(), "");
        } finally {
            List<Session> sessions = client.getSessionList(QueryParams.DEFAULT).getValue();
            String sessionId = null;
            if (sessions != null && !sessions.isEmpty()) {
                for (Session session : sessions) {
                    if (session.getName().equals(ephemralNode.getServerInfo())) {
                        sessionId = session.getId();
                    } else {
                        sessionId = generateNewSession(ephemralNode);
                    }
                }
            } else {
                sessionId = generateNewSession(ephemralNode);
            }
            PutParams kvPutParams = new PutParams();
            kvPutParams.setAcquireSession(sessionId);
            client.setKVValue(ephemralNode.getKey(), ephemralNode.getServerInfo(), kvPutParams);
        }
    }

    private String generateNewSession(SalukiConsulEphemralNode ephemralNode) {
        String sessionId;
        NewSession newSession = ephemralNode.getNewSession();
        sessionId = client.sessionCreate(newSession, QueryParams.DEFAULT).getValue();
        ttlScheduler.addHeartbeatSession(sessionId);
        return sessionId;
    }

    public SalukiConsulServiceResp lookupHealthService(String serviceName, long lastConsulIndex) {
        QueryParams queryParams = new QueryParams(ConsulConstants.CONSUL_BLOCK_TIME_SECONDS, lastConsulIndex);
        Response<List<HealthService>> orgResponse = client.getHealthServices(serviceName, true, queryParams);
        if (orgResponse != null && orgResponse.getValue() != null && !orgResponse.getValue().isEmpty()) {
            List<HealthService> HealthServices = orgResponse.getValue();
            List<SalukiConsulService> ConsulServcies = Lists.newArrayList();
            for (HealthService orgService : HealthServices) {
                Service org = orgService.getService();
                SalukiConsulService newService = SalukiConsulService.newSalukiService()//
                                                                    .withAddress(org.getAddress())//
                                                                    .withName(org.getService())//
                                                                    .withId(org.getId())//
                                                                    .withPort(org.getPort().toString())//
                                                                    .withTags(org.getTags())//
                                                                    .build();
                ConsulServcies.add(newService);
            }
            if (!ConsulServcies.isEmpty()) {
                SalukiConsulServiceResp response = SalukiConsulServiceResp.newResponse()//
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
