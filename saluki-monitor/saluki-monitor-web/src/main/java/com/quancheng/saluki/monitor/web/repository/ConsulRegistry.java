package com.quancheng.saluki.monitor.web.repository;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Check;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quancheng.saluki.monitor.domain.SalukiApplication;
import com.quancheng.saluki.monitor.web.utils.NamedThreadFactory;

@Repository
public class ConsulRegistry {

    private static final Logger                     logger                    = LoggerFactory.getLogger(ConsulRegistry.class);

    public static final String                      CONSUL_SERVICE_PRE        = "Saluki_";

    private final Gson                              gson                      = new Gson();

    private String                                  agentHost;

    private int                                     agentPort;

    private ConsulClient                            consulClient;

    private final Map<String, Pair<String, String>> serviceApplicationMapping = Maps.newConcurrentMap();

    private final Map<String, Set<String>>          applicationHostsMapping   = Maps.newConcurrentMap();

    private final ScheduledExecutorService          executor                  = Executors.newScheduledThreadPool(1,
                                                                                                                 new NamedThreadFactory("ConsulLookUpService",
                                                                                                                                        true));;

    @PostConstruct
    public void init() {
        consulClient = new ConsulClient(agentHost, agentPort);
        executor.scheduleWithFixedDelay(new LookUpService(), 0, 1, TimeUnit.HOURS);
    }

    public List<SalukiApplication> getAllApplication() {
        // 首先做一次剔重操作
        Set<Pair<String, String>> consumerAndProviderAppNames = Sets.newHashSet();
        for (Map.Entry<String, Pair<String, String>> entry : serviceApplicationMapping.entrySet()) {
            Pair<String, String> consumerAndProviderApp = entry.getValue();
            consumerAndProviderAppNames.add(consumerAndProviderApp);
        }
        // 取出consumer和provider
        Iterator<Pair<String, String>> it = consumerAndProviderAppNames.iterator();
        while (it.hasNext()) {
            Pair<String, String> consumerAndProviderApp = it.next();
            String consumerAppName = consumerAndProviderApp.getLeft();
            String providerAppName = consumerAndProviderApp.getRight();
            Set<String> consumerHost = applicationHostsMapping.get(consumerAppName);
            Set<String> providerHost = applicationHostsMapping.get(providerAppName);
        }
        return null;
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

    private class LookUpService implements Runnable {

        @Override
        public void run() {
            Map<String, Check> allServices = consulClient.getAgentChecks().getValue();
            for (Map.Entry<String, Check> entry : allServices.entrySet()) {
                Check serviceCheck = entry.getValue();
                if (serviceCheck.getStatus() == Check.CheckStatus.PASSING) {
                    String group = serviceCheck.getServiceName();
                    String[] args = serviceCheck.getServiceId().split("-");
                    String serviceName = args[1];
                    Pair<String, String> serviceAppPair = addServiceToCache(group, serviceName);
                    // 这里需要把应用名加上，防止serviceName重复(是否必要？)
                    String serviceKey = StringUtils.replace(group, CONSUL_SERVICE_PRE, "") + ":" + serviceName;
                    serviceApplicationMapping.put(serviceKey, serviceAppPair);
                }
            }
        }

        private Pair<String, String> addServiceToCache(String group, String serviceName) {
            String serviceKey = group + "/" + serviceName;
            List<String> providerAndConsumers = consulClient.getKVKeysOnly(serviceKey).getValue();
            String providerAppName = null;
            String consumerAppName = null;
            Set<String> providerHosts = Sets.newHashSet();
            Set<String> comsumerHosts = Sets.newHashSet();
            for (String providerAndConsumer : providerAndConsumers) {
                String providerAndConsumerAppInfo = consulClient.getKVValue(providerAndConsumer).getValue().getDecodedValue();
                Map<String, String> appInfoMap = gson.fromJson(providerAndConsumerAppInfo,
                                                               new TypeToken<Map<String, String>>() {
                                                               }.getType());
                String[] serverInfos = StringUtils.split(StringUtils.remove(providerAndConsumer, serviceKey + "/"),
                                                         "/");
                String appFlag = serverInfos[0];
                String appHost = serverInfos[1];
                // 对于provider端，直接取group做为应用名
                if (appFlag.equals("provider")) {
                    String appName = StringUtils.replace(group, CONSUL_SERVICE_PRE, "");
                    providerAppName = appName;
                    providerHosts.add(appHost);
                } // 对于consumer端，需要取注册的参数做为应用名
                else if (appFlag.equals("consumer")) {
                    String appName = appInfoMap.get("appName");
                    consumerAppName = appName;
                    comsumerHosts.add(appHost);
                }
            }
            if (providerAppName == null || consumerAppName == null) {
                logger.error(serviceKey + " have not registry appName");
            }
            if (applicationHostsMapping.containsKey(providerAppName)) {
                applicationHostsMapping.get(providerAppName).addAll(providerHosts);
            } else {
                applicationHostsMapping.put(providerAppName, providerHosts);
            }
            if (applicationHostsMapping.containsKey(consumerAppName)) {
                applicationHostsMapping.get(consumerAppName).addAll(comsumerHosts);
            } else {
                applicationHostsMapping.put(consumerAppName, comsumerHosts);
            }
            return new ImmutablePair<String, String>(consumerAppName, providerAppName);
        }

    }
}
