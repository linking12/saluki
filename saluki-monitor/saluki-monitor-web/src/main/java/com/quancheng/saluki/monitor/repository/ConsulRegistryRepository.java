package com.quancheng.saluki.monitor.repository;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Check;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.quancheng.saluki.core.utils.NamedThreadFactory;
import com.quancheng.saluki.monitor.SalukiHost;

@Repository
public class ConsulRegistryRepository {

    public static final String                                        CONSUL_SERVICE_PRE = "Saluki_";

    private final Gson                                                gson               = new Gson();

    @Value("${saluki.monitor.consulhost}")
    private String                                                    agentHost;

    @Value("${saluki.monitor.consulport}")
    private int                                                       agentPort;

    private ConsulClient                                              consulClient;

    private final Map<String, Pair<Set<SalukiHost>, Set<SalukiHost>>> servicesPassing    = Maps.newConcurrentMap();

    private final Map<String, Pair<Set<SalukiHost>, Set<SalukiHost>>> servicesFailing    = Maps.newConcurrentMap();

    private final ScheduledExecutorService                            executor           = Executors.newScheduledThreadPool(1,
                                                                                                                            new NamedThreadFactory("ConsulLookUpService",
                                                                                                                                                   true));;

    @PostConstruct
    public void init() {
        consulClient = new ConsulClient(agentHost, agentPort);
        executor.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                loadAllServiceFromConsul();
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    public void loadAllServiceFromConsul() {
        Map<String, Check> allServices = consulClient.getAgentChecks().getValue();
        for (Map.Entry<String, Check> entry : allServices.entrySet()) {
            Check serviceCheck = entry.getValue();
            String group = serviceCheck.getServiceName();
            if (StringUtils.startsWith(group, CONSUL_SERVICE_PRE)) {
                Triple<String, String, String> hostPortServiceVersion = getPortHostService(serviceCheck.getServiceId());
                String hostRpcPort = hostPortServiceVersion.getLeft();
                String service = hostPortServiceVersion.getMiddle();
                String version = hostPortServiceVersion.getRight();
                String serviceKey = generateServicekey(group, service, version);
                if (serviceCheck.getStatus() == Check.CheckStatus.PASSING) {
                    Pair<Set<SalukiHost>, Set<SalukiHost>> providerAndConsumer = getProviderAndConsumer(group, service,
                                                                                                        version);
                    if (providerAndConsumer != null) {
                        servicesPassing.put(serviceKey, providerAndConsumer);
                    }
                } else {
                    Pair<Set<SalukiHost>, Set<SalukiHost>> providerAndConsumer = servicesFailing.get(serviceKey);
                    SalukiHost providerHost = new SalukiHost(hostRpcPort, "0");
                    providerHost.setStatus("failing");
                    providerHost.setUrl("service:" + hostRpcPort + "-" + service);
                    if (servicesFailing.get(serviceKey) == null) {
                        Set<SalukiHost> provider = Sets.newHashSet(providerHost);
                        providerAndConsumer = new ImmutablePair<Set<SalukiHost>, Set<SalukiHost>>(provider, null);
                        servicesFailing.put(serviceKey, providerAndConsumer);
                    }
                    providerAndConsumer.getLeft().add(providerHost);
                }
            }

        }
    }

    public Map<String, Pair<Set<SalukiHost>, Set<SalukiHost>>> getAllPassingService() {
        return this.servicesPassing;
    }

    public Map<String, Pair<Set<SalukiHost>, Set<SalukiHost>>> getAllFailingService() {
        return this.servicesFailing;
    }

    private Pair<Set<SalukiHost>, Set<SalukiHost>> getProviderAndConsumer(String group, String service,
                                                                          String version) {
        Set<SalukiHost> providerHosts = Sets.newHashSet();
        Set<SalukiHost> comsumerHosts = Sets.newHashSet();
        List<String> providerAndConsumerKvs = consulClient.getKVKeysOnly(group + "/" + service + "/"
                                                                         + version).getValue();
        if (providerAndConsumerKvs != null) {
            for (String providerAndConsumerKv : providerAndConsumerKvs) {
                Triple<String, String, String> machineInfo = getmachineInfo(providerAndConsumerKv,
                                                                            group + "/" + service);
                String appFlag = machineInfo.getLeft();
                String[] appHostRpcPort = machineInfo.getMiddle().split(":");
                String appHttpPort = machineInfo.getRight();
                // 对于provider端，直接取group做为应用名
                if (appFlag.equals("provider")) {
                    SalukiHost host = new SalukiHost(appHostRpcPort[0], appHttpPort, appHostRpcPort[1]);
                    host.setStatus("passing");
                    host.setUrl("service:" + appHostRpcPort[0] + ":" + appHostRpcPort[1] + "-" + service);
                    providerHosts.add(host);
                } // 对于consumer端，需要取注册的参数做为应用名
                else if (appFlag.equals("consumer")) {
                    SalukiHost host = new SalukiHost(appHostRpcPort[0], appHttpPort, "0");
                    host.setStatus("passing");
                    host.setUrl(null);
                    comsumerHosts.add(host);
                }
            }
            return new ImmutablePair<Set<SalukiHost>, Set<SalukiHost>>(providerHosts, comsumerHosts);
        }
        return null;
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
        String version = serverInfos[0];
        String machineFlag = serverInfos[1];
        String machineIpAndRpcPort = serverInfos[2];
        String machineHttpPort = machineInfo.get("serverHttpPort");
        return new ImmutableTriple<String, String, String>(machineFlag, machineIpAndRpcPort, machineHttpPort);
    }

    private Triple<String, String, String> getPortHostService(String serviceId) {
        String[] args = StringUtils.split(serviceId, "-");
        String hostRpcPort = args[0];
        String service = args[1];
        String version = "1.0.0";
        if (args.length > 2) {
            version = args[2];
        }
        return new ImmutableTriple<String, String, String>(hostRpcPort, service, version);
    }

    private String generateServicekey(String group, String service, String version) {
        // 含义是在某一个应用下有某一个服务,其版本是多少
        return StringUtils.remove(group, CONSUL_SERVICE_PRE) + ":" + service + ":" + version;
    }

    /**
     * ==============help method=============
     */

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
