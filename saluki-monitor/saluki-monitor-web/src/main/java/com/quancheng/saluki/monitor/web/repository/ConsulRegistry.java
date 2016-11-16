package com.quancheng.saluki.monitor.web.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.quancheng.saluki.monitor.domain.SalukiService;
import com.quancheng.saluki.monitor.web.utils.NamedThreadFactory;

@Repository
public class ConsulRegistry {

    private static final Logger                               logger                      = LoggerFactory.getLogger(ConsulRegistry.class);

    public static final String                                CONSUL_SERVICE_PRE          = "Saluki_";

    private final Gson                                        gson                        = new Gson();

    private String                                            agentHost;

    private int                                               agentPort;

    private ConsulClient                                      consulClient;

    private final Map<String, Triple<String, String, String>> servicesPassingApplications = Maps.newConcurrentMap();

    private final Map<String, Triple<String, String, String>> servicesFailingApplications = Maps.newConcurrentMap();

    private final Map<String, Set<SalukiHost>>                applicationPassingHost      = Maps.newConcurrentMap();

    private final Map<String, Set<SalukiHost>>                applicationFailingHost      = Maps.newConcurrentMap();

    private final ScheduledExecutorService                    executor                    = Executors.newScheduledThreadPool(1,
                                                                                                                             new NamedThreadFactory("ConsulLookUpService",
                                                                                                                                                    true));;

    @PostConstruct
    public void init() {
        consulClient = new ConsulClient(agentHost, agentPort);
        executor.scheduleWithFixedDelay(new LookUpService(), 0, 1, TimeUnit.HOURS);
    }

    public List<SalukiApplication> getAllApplication() {
        Map<String, SalukiApplication> apps = Maps.newConcurrentMap();
        for (Map.Entry<String, Triple<String, String, String>> entry : servicesPassingApplications.entrySet()) {
            SalukiApplication app = this.pickUpApp(entry.getKey(), entry.getValue(), true);
            if (apps.get(app.getAppName()) == null) {
                apps.put(app.getAppName(), app);
            } else {
                apps.get(app.getAppName()).addServices(app.getServices());
            }
        }
        for (Map.Entry<String, Triple<String, String, String>> entry : servicesFailingApplications.entrySet()) {
            SalukiApplication app = this.pickUpApp(entry.getKey(), entry.getValue(), false);
            if (apps.get(app.getAppName()) == null) {
                apps.put(app.getAppName(), app);
            } else {
                apps.get(app.getAppName()).addServices(app.getServices());
            }
        }
        List<SalukiApplication> applications = Lists.newArrayList();
        for (Map.Entry<String, SalukiApplication> entry : apps.entrySet()) {
            applications.add(entry.getValue());
        }
        return applications;
    }

    private SalukiApplication pickUpApp(String serivceKey, Triple<String, String, String> consumerProviderStatus,
                                        Boolean isPassing) {
        String[] serviceKey_ = StringUtils.split(serivceKey, ":");
        String appName = serviceKey_[0];
        String serviceName = serviceKey_[1];
        String consumerAppName = consumerProviderStatus.getLeft();
        String providerAppName = consumerProviderStatus.getRight();
        String status = consumerProviderStatus.getMiddle();
        if (isPassing) {
            Set<SalukiHost> consumerHosts = applicationPassingHost.get(consumerAppName);
            Set<SalukiHost> providerHosts = applicationPassingHost.get(providerAppName);
            SalukiApplication app = new SalukiApplication(appName);
            SalukiService service = new SalukiService(serviceName);
            service.setStatus(status);
            service.addConsumerHosts(consumerHosts);
            service.addPrividerHost(providerHosts);
            app.addService(service);
            return app;
        } else {
            Set<SalukiHost> providerHosts = applicationFailingHost.get(providerAppName);
            SalukiApplication app = new SalukiApplication(appName);
            SalukiService service = new SalukiService(serviceName);
            service.setStatus(status);
            service.addPrividerHost(providerHosts);
            app.addService(service);
            return app;
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

    private class LookUpService implements Runnable {

        @Override
        public void run() {
            Map<String, Check> allServices = consulClient.getAgentChecks().getValue();
            for (Map.Entry<String, Check> entry : allServices.entrySet()) {
                Check serviceCheck = entry.getValue();
                String group = serviceCheck.getServiceName();
                String[] args = serviceCheck.getServiceId().split("-");
                String serviceRpcPort = args[0];
                String serviceName = args[1];
                String serviceKey = StringUtils.replace(group, CONSUL_SERVICE_PRE, "") + ":" + serviceName;
                if (serviceCheck.getStatus() == Check.CheckStatus.PASSING) {
                    Triple<String, String, String> serviceAppTriple = addPassingServiceToCache(group, serviceName,
                                                                                               Check.CheckStatus.PASSING.name(),
                                                                                               serviceRpcPort);
                    servicesPassingApplications.put(serviceKey, serviceAppTriple);
                } else {
                    String appName = StringUtils.replace(group, CONSUL_SERVICE_PRE, "");
                    Triple<String, String, String> serviceAppTriple = new ImmutableTriple<String, String, String>(null,
                                                                                                                  serviceCheck.getStatus().name(),
                                                                                                                  appName);
                    if (applicationFailingHost.get(appName) == null) {
                        Set<SalukiHost> host = Sets.newHashSet();
                        applicationFailingHost.put(appName, host);
                    }
                    applicationFailingHost.get(appName).add(new SalukiHost(serviceRpcPort));
                    servicesFailingApplications.put(serviceKey, serviceAppTriple);
                }
            }
        }

        private Triple<String, String, String> addPassingServiceToCache(String group, String serviceName,
                                                                        String serviceStatus, String serviceRpcPort) {
            String serviceKey = group + "/" + serviceName;
            List<String> providerAndConsumers = consulClient.getKVKeysOnly(serviceKey).getValue();
            String providerAppName = null;
            String consumerAppName = null;
            Set<SalukiHost> providerHosts = Sets.newHashSet();
            Set<SalukiHost> comsumerHosts = Sets.newHashSet();
            for (String providerAndConsumer : providerAndConsumers) {
                String providerAndConsumerAppInfo = consulClient.getKVValue(providerAndConsumer).getValue().getDecodedValue();
                Map<String, String> appInfoMap = gson.fromJson(providerAndConsumerAppInfo,
                                                               new TypeToken<Map<String, String>>() {
                                                               }.getType());
                String[] serverInfos = StringUtils.split(StringUtils.remove(providerAndConsumer, serviceKey + "/"),
                                                         "/");
                String appFlag = serverInfos[0];
                String appHost = serverInfos[1];
                String appHttpPort = appInfoMap.get("serverHttpPort");
                // 对于provider端，直接取group做为应用名
                if (appFlag.equals("provider")) {
                    String appName = StringUtils.replace(group, CONSUL_SERVICE_PRE, "");
                    providerAppName = appName;
                    String[] rpcPort = StringUtils.split(serviceRpcPort, ":");
                    providerHosts.add(new SalukiHost(appHost, appHttpPort,
                                                     rpcPort.length > 1 ? rpcPort[1] : rpcPort[0]));
                } // 对于consumer端，需要取注册的参数做为应用名
                else if (appFlag.equals("consumer")) {
                    String appName = appInfoMap.get("appName");
                    consumerAppName = appName;
                    comsumerHosts.add(new SalukiHost(appHost, appHttpPort, "0"));
                }
            }
            if (providerAppName == null || consumerAppName == null) {
                logger.error(serviceKey + " have not registry appName");
            }
            if (applicationPassingHost.containsKey(providerAppName)) {
                applicationPassingHost.get(providerAppName).addAll(providerHosts);
            } else {
                applicationPassingHost.put(providerAppName, providerHosts);
            }
            if (applicationPassingHost.containsKey(consumerAppName)) {
                applicationPassingHost.get(consumerAppName).addAll(comsumerHosts);
            } else {
                applicationPassingHost.put(consumerAppName, comsumerHosts);
            }
            return new ImmutableTriple<String, String, String>(consumerAppName, serviceStatus, providerAppName);
        }

    }
}
