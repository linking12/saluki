package com.quancheng.saluki.monitor.web.repository;

import java.net.URLDecoder;
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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quancheng.saluki.monitor.domain.SalukiApplication;
import com.quancheng.saluki.monitor.domain.SalukiHost;

@Repository
public class ConsulRegistry {

    public static final String                       CONSUL_SERVICE_PRE  = "Saluki_";

    private final Gson                               gson                = new Gson();

    private String                                   agentHost;

    private int                                      agentPort;

    private ConsulClient                             consulClient;

    private final ConcurrentMap<String, Set<String>> applicatonProviders = new ConcurrentHashMap<String, Set<String>>();

    private final Map<String, SalukiApplication>     applicatonConsumers = Maps.newConcurrentMap();

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
                    String consumerParam = StringUtils.substring(consumer_host_kv,
                                                                 consumer_host_kv.lastIndexOf("/") + 1);
                    Map<String, String> consumerInfo = gson.fromJson(URLDecoder.decode(consumerParam, "UTF-8"),
                                                                     new TypeToken<Map<String, String>>() {
                                                                     }.getType());
                    String appName = consumerInfo.get("appName");
                    String appHost = consumerInfo.get("consumerHost");
                    String appPort = consumerInfo.get("consumerPort");
                    SalukiApplication application = new SalukiApplication(appName);
                    application.addHost(new SalukiHost(appHost, appPort));
                    consumer_host = clientParam.get("consumerHost");

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

    private void addConsumer(SalukiApplication app) {
        String appName = app.getName();
        if (applicatonConsumers.containsKey(appName)) {
            SalukiApplication application = applicatonConsumers.get(appName);
            application.addAllChild(app.getChildren());
            application.addAllHost(app.getHosts());
        }

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
