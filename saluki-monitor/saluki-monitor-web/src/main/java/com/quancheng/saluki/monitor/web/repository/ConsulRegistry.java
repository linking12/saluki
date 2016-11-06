package com.quancheng.saluki.monitor.web.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Check;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.quancheng.saluki.monitor.domain.SalukiApplication;

@Repository
public class ConsulRegistry {

    public static final String                       CONSUL_SERVICE_PRE  = "Saluki_";

    private String                                   agentHost;

    private int                                      agentPort;

    private ConsulClient                             consulClient;

    private final ConcurrentMap<String, Set<String>> applicatonProviders = new ConcurrentHashMap<String, Set<String>>();

    private final ConcurrentMap<String, Set<String>> applicatonConsumers = new ConcurrentHashMap<String, Set<String>>();

    @PostConstruct
    public void init() {
        consulClient = new ConsulClient(agentHost, agentPort);
    }

    public List<SalukiApplication> getAllApplication() {
        List<SalukiApplication> applications = Lists.newArrayList();
        Map<String, Check> allServices = consulClient.getAgentChecks().getValue();
        for (Map.Entry<String, Check> entry : allServices.entrySet()) {
            Check serviceCheck = entry.getValue();
            if (serviceCheck.getStatus() == Check.CheckStatus.PASSING) {
                String group = serviceCheck.getServiceName();
                String[] args = serviceCheck.getServiceId().split("-");
                String provider_host = args[0];
                String serviceName = args[1];
                getProviders(group).add(provider_host);
                List<String> consumer_hosts_kvs = consulClient.getKVKeysOnly(group + "/" + serviceName).getValue();
                for (String consumer_host_kv : consumer_hosts_kvs) {
                    String consumer_port = consulClient.getKVValue(consumer_host_kv).getValue().getDecodedValue();
                    String consumer_host = StringUtils.substring(consumer_host_kv,
                                                                 consumer_host_kv.lastIndexOf("/") + 1);
                    getConsumers(group).add(consumer_host + ":" + consumer_port);
                }
            }
        }
        for (Map.Entry<String, Set<String>> entry : applicatonProviders.entrySet()) {
            String group = entry.getKey();
            Set<String> providerHost = entry.getValue();
            Set<String> consumerHost = applicatonConsumers.get(group);
            SalukiApplication app = new SalukiApplication();
            app.setName(StringUtils.replace(group, CONSUL_SERVICE_PRE, ""));
            app.setProviders(providerHost);
            app.setConsumers(consumerHost);
            applications.add(app);
        }
        return applications;
    }

    private Set<String> getProviders(String applicationName) {
        Set<String> providers = applicatonProviders.get(applicationName);
        if (providers == null) {
            applicatonProviders.putIfAbsent(applicationName, Sets.newHashSet());
            providers = applicatonProviders.get(applicationName);
        }
        return providers;
    }

    private Set<String> getConsumers(String applicationName) {
        Set<String> consumers = applicatonConsumers.get(applicationName);
        if (consumers == null) {
            applicatonConsumers.putIfAbsent(applicationName, Sets.newHashSet());
            consumers = applicatonConsumers.get(applicationName);
        }
        return consumers;
    }

    public String getAgentHost() {
        return agentHost;
    }

    public void setAgentHost(String agentHost) {
        this.agentHost = agentHost;
    }

    public int getAgentPort() {
        return agentPort;
    }

    public void setAgentPort(int agentPort) {
        this.agentPort = agentPort;
    }

}
