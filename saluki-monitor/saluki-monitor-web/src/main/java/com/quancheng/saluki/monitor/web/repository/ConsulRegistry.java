package com.quancheng.saluki.monitor.web.repository;

import java.util.HashSet;
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
import com.quancheng.saluki.monitor.web.utils.NamedThreadFactory;

@Repository
public class ConsulRegistry {

    public static final String                                                      CONSUL_SERVICE_PRE = "Saluki_";

    private final Gson                                                              gson               = new Gson();

    private String                                                                  agentHost;

    private int                                                                     agentPort;

    private ConsulClient                                                            consulClient;

    private final Map<String, Pair<Set<SalukiApplication>, Set<SalukiApplication>>> services           = Maps.newConcurrentMap();

    private final ScheduledExecutorService                                          executor           = Executors.newScheduledThreadPool(1,
                                                                                                                                          new NamedThreadFactory("ConsulLookUpService",
                                                                                                                                                                 true));;

    @PostConstruct
    public void init() {
        consulClient = new ConsulClient(agentHost, agentPort);
        executor.scheduleWithFixedDelay(new LookUpService(), 0, 1, TimeUnit.HOURS);
    }

    public List<SalukiApplication> getAllApplication() {
        if (services.isEmpty()) {
            return Lists.newArrayList();
        } else {
            Map<String, Set<SalukiApplication>> apps = Maps.newConcurrentMap();
            for (Map.Entry<String, Pair<Set<SalukiApplication>, Set<SalukiApplication>>> entry : services.entrySet()) {
                Pair<Set<SalukiApplication>, Set<SalukiApplication>> serviceApps = entry.getValue();
                Set<SalukiApplication> consumers = serviceApps.getLeft();
                Set<SalukiApplication> providers = serviceApps.getRight();
                int consumerIndex = 0;
                for (Iterator<SalukiApplication> it = consumers.iterator(); it.hasNext();) {
                    SalukiApplication consumerApp = it.next();
                    if (consumerIndex == 0) {
                        String consumerAppName = consumerApp.getName();
                        Set<SalukiApplication> appName_Set = apps.get(consumerAppName);
                        if (appName_Set == null) {
                            appName_Set = Sets.newHashSet();
                        }
                        appName_Set.add(consumerApp);
                    } else {

                    }
                    consumerIndex++;
                }

            }
        }
    }

    public void pickUpApplication() {

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
                    String serviceKey = group + "/" + serviceName;
                    addServiceToCache(serviceKey);
                }
            }
        }

        private void addServiceToCache(String serviceKey) {
            List<String> applicationPaths = consulClient.getKVKeysOnly(serviceKey).getValue();
            Set<SalukiApplication> providers = Sets.newHashSet();
            Set<SalukiApplication> consumers = Sets.newHashSet();
            for (String applicationPath : applicationPaths) {
                lookUpAppinfo(serviceKey, providers, consumers, applicationPath);
            }
            for (SalukiApplication consumer : consumers) {
                consumer.addAllParent(providers);
            }
            for (SalukiApplication provider : providers) {
                provider.addAllChild(consumers);
            }
            services.put(serviceKey,
                         new ImmutablePair<Set<SalukiApplication>, Set<SalukiApplication>>(consumers, providers));
        }

        private void lookUpAppinfo(String serviceKey, Set<SalukiApplication> providers,
                                   Set<SalukiApplication> consumers, String applicationPath) {
            String applicationInfo = consulClient.getKVValue(applicationPath).getValue().getDecodedValue();
            Map<String, String> serverParam = gson.fromJson(applicationInfo, new TypeToken<Map<String, String>>() {
            }.getType());
            String appName = serverParam.get("appName");
            String appHttpPort = serverParam.get("serverHttpPort");
            String[] serverInfos = StringUtils.split(StringUtils.remove(applicationPath, serviceKey + "/"), "/");
            String appFlag = serverInfos[0];
            String appHost = serverInfos[1];
            SalukiApplication application = new SalukiApplication(appName);
            application.addHost(new SalukiHost(appHost, appHttpPort));
            if (appFlag.equals("provider")) {
                application.setServerFlag("provider");
                providers.add(application);
            } else if (appFlag.equals("consumer")) {
                application.setServerFlag("consumer");
                consumers.add(application);
            }
        }
    }
}
