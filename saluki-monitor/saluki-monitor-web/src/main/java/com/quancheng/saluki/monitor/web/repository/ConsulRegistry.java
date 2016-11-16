package com.quancheng.saluki.monitor.web.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Repository;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Check;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.quancheng.saluki.monitor.domain.SalukiApplication;
import com.quancheng.saluki.monitor.domain.SalukiHost;
import com.quancheng.saluki.monitor.domain.SalukiService;
import com.quancheng.saluki.monitor.web.utils.ConcurrentReferenceHashMap;
import com.quancheng.saluki.monitor.web.utils.NamedThreadFactory;

@Repository
public class ConsulRegistry {

    public static final String                                        CONSUL_SERVICE_PRE = "Saluki_";

    private final Gson                                                gson               = new Gson();

    private String                                                    agentHost;

    private int                                                       agentPort;

    private ConsulClient                                              consulClient;

    private final Map<String, Pair<Set<SalukiHost>, Set<SalukiHost>>> servicesPassing    = new ConcurrentReferenceHashMap<String, Pair<Set<SalukiHost>, Set<SalukiHost>>>();

    private final Map<String, Pair<Set<SalukiHost>, Set<SalukiHost>>> servicesFailing    = new ConcurrentReferenceHashMap<String, Pair<Set<SalukiHost>, Set<SalukiHost>>>();

    private final ScheduledExecutorService                            executor           = Executors.newScheduledThreadPool(1,
                                                                                                                            new NamedThreadFactory("ConsulLookUpService",
                                                                                                                                                   true));;

    @PostConstruct
    public void init() {
        consulClient = new ConsulClient(agentHost, agentPort);
        executor.scheduleWithFixedDelay(new LookUpService(), 0, 1, TimeUnit.HOURS);
    }

    public List<SalukiApplication> getAllApplication() {
        Map<String, SalukiApplication> appCache = Maps.newHashMap();
        for (Map.Entry<String, Pair<Set<SalukiHost>, Set<SalukiHost>>> entry : servicesPassing.entrySet()) {
            processApplication(appCache, entry, true);
        }
        for (Map.Entry<String, Pair<Set<SalukiHost>, Set<SalukiHost>>> entry : servicesFailing.entrySet()) {
            processApplication(appCache, entry, false);
        }
        List<SalukiApplication> applications = Lists.newArrayList();
        for (Map.Entry<String, SalukiApplication> entry : appCache.entrySet()) {
            applications.add(entry.getValue());
        }
        return applications;
    }

    private void processApplication(Map<String, SalukiApplication> appCache,
                                    Map.Entry<String, Pair<Set<SalukiHost>, Set<SalukiHost>>> entry,
                                    Boolean passOrFail) {
        Pair<String, String> appNameService = getAppNameService(entry.getKey());
        Pair<Set<SalukiHost>, Set<SalukiHost>> providerConsumer = entry.getValue();
        String appName = appNameService.getLeft();
        String serviceName = appNameService.getRight();
        SalukiApplication application = new SalukiApplication(appName);
        SalukiService service = new SalukiService(serviceName);
        service.addPrividerHost(providerConsumer.getLeft());
        if (passOrFail) {
            service.addConsumerHosts(providerConsumer.getRight());
            service.setStatus("passing");
        } else {
            service.setStatus("failing");
        }
        application.addService(service);
        if (appCache.get(application.getAppName()) == null) {
            appCache.put(application.getAppName(), application);
        } else {
            appCache.get(application.getAppName()).addServices(application.getServices());
        }
    }

    private Pair<Set<SalukiHost>, Set<SalukiHost>> getProviderAndConsumer(String group, String service) {
        Set<SalukiHost> providerHosts = Sets.newHashSet();
        Set<SalukiHost> comsumerHosts = Sets.newHashSet();
        List<String> providerAndConsumerKvs = consulClient.getKVKeysOnly(group + "/" + service).getValue();
        for (String providerAndConsumerKv : providerAndConsumerKvs) {
            Triple<String, String, String> machineInfo = getmachineInfo(providerAndConsumerKv, group + "/" + service);
            String appFlag = machineInfo.getLeft();
            String[] appHostRpcPort = machineInfo.getMiddle().split(":");
            String appHttpPort = machineInfo.getRight();
            // 对于provider端，直接取group做为应用名
            if (appFlag.equals("provider")) {
                providerHosts.add(new SalukiHost(appHostRpcPort[0], appHttpPort, appHostRpcPort[1]));
            } // 对于consumer端，需要取注册的参数做为应用名
            else if (appFlag.equals("consumer")) {
                comsumerHosts.add(new SalukiHost(appHostRpcPort[0], appHttpPort, "0"));
            }
        }
        return new ImmutablePair<Set<SalukiHost>, Set<SalukiHost>>(providerHosts, comsumerHosts);
    }

    /**
     * ==============help method=============
     */
    private Triple<String, String, String> getmachineInfo(String providerAndConsumerKv, String groupService) {
        String serverInfo = consulClient.getKVValue(providerAndConsumerKv).getValue().getDecodedValue();
        @SuppressWarnings("unchecked")
        Map<String, String> machineInfo = gson.fromJson(serverInfo, Map.class);
        String flagAndIp = StringUtils.remove(providerAndConsumerKv, groupService + "/");
        String[] serverInfos = StringUtils.split(flagAndIp, "/");
        String machineFlag = serverInfos[0];
        String machineIpAndRpcPort = serverInfos[1];
        String machineHttpPort = machineInfo.get("serverHttpPort");
        return new ImmutableTriple<String, String, String>(machineFlag, machineIpAndRpcPort, machineHttpPort);
    }

    private Triple<String, String, String> getPortHostService(String serviceId) {
        String[] args = serviceId.split("-");
        String[] hostRpcPort = StringUtils.split(args[0], ":");
        String host = hostRpcPort[0];
        String port = hostRpcPort[1];
        String service = args[1];
        return new ImmutableTriple<String, String, String>(port, host, service);
    }

    private String generateServicekey(String group, String service) {
        // 含义是在某一个应用下有某一个服务
        return StringUtils.remove(group, CONSUL_SERVICE_PRE) + ":" + service;
    }

    private Pair<String, String> getAppNameService(String serviceKey) {
        String[] args = serviceKey.split(":");
        return new ImmutablePair<String, String>(args[0], args[1]);
    }

    /**
     * ==============help method=============
     */
    private class LookUpService implements Runnable {

        @Override
        public void run() {
            Map<String, Check> allServices = consulClient.getAgentChecks().getValue();
            for (Map.Entry<String, Check> entry : allServices.entrySet()) {
                Check serviceCheck = entry.getValue();
                String group = serviceCheck.getServiceName();
                Triple<String, String, String> PortHostService = getPortHostService(serviceCheck.getServiceId());
                String host = PortHostService.getMiddle();
                String rpcPort = PortHostService.getLeft();
                String service = PortHostService.getRight();
                String serviceKey = generateServicekey(group, service);
                if (serviceCheck.getStatus() == Check.CheckStatus.PASSING) {
                    Pair<Set<SalukiHost>, Set<SalukiHost>> providerAndConsumer = getProviderAndConsumer(group, service);
                    servicesPassing.put(serviceKey, providerAndConsumer);
                } else {
                    Pair<Set<SalukiHost>, Set<SalukiHost>> providerAndConsumer = servicesFailing.get(serviceKey);
                    if (servicesFailing.get(serviceKey) == null) {
                        Set<SalukiHost> provider = Sets.newHashSet(new SalukiHost(host, null, rpcPort));
                        providerAndConsumer = new ImmutablePair<Set<SalukiHost>, Set<SalukiHost>>(provider, null);
                        servicesFailing.put(serviceKey, providerAndConsumer);
                    }
                    providerAndConsumer.getLeft().add(new SalukiHost(host, null, rpcPort));
                }
            }
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
