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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Check;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.domain.GrpcHost;
@Repository
public class ConsulRegistryRepository {
    private static final Logger                                   log             = Logger.getLogger(ConsulRegistryRepository.class);
    @Value("${saluki.monitor.consulhost}")
    private String                                                agentHost;
    private ConsulClient                                          consulClient;
    private final Map<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> servicesPassing = Maps.newConcurrentMap();
    private final Map<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> servicesFailing = Maps.newConcurrentMap();
    private final ScheduledExecutorService                        executor        = Executors.newScheduledThreadPool(1,
                                                                                                                     new NamedThreadFactory("ConsulLookUpService",
                                                                                                                                            true));;
    @PostConstruct
    public void init() {
        consulClient = new ConsulClient(agentHost);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("begin to load from registry");
                    servicesPassing.clear();
                    servicesFailing.clear();
                    loadAllServiceFromConsul();
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }
    public void loadAllServiceFromConsul() {
        Map<String, Check> allServices = consulClient.getAgentChecks().getValue();
        for (Map.Entry<String, Check> entry : allServices.entrySet()) {
            Check serviceCheck = entry.getValue();
            String group = serviceCheck.getServiceName();
            if (StringUtils.startsWith(group, Constants.CONSUL_SERVICE_PRE)) {
                Triple<String, String, String> hostPortServiceVersion = getPortHostService(serviceCheck.getServiceId());
                String hostRpcPort = hostPortServiceVersion.getLeft();
                String service = hostPortServiceVersion.getMiddle();
                String version = hostPortServiceVersion.getRight();
                String serviceKey = generateServicekey(group, service, version);
                if (serviceCheck.getStatus() == Check.CheckStatus.PASSING) {
                    Pair<Set<GrpcHost>, Set<GrpcHost>> providerAndConsumer = getProviderAndConsumer(group, service,
                                                                                                    version);
                    if (providerAndConsumer != null) {
                        servicesPassing.put(serviceKey, providerAndConsumer);
                    }
                } else {
                    Pair<Set<GrpcHost>, Set<GrpcHost>> providerAndConsumer = servicesFailing.get(serviceKey);
                    GrpcHost providerHost = new GrpcHost(hostRpcPort, "0");
                    providerHost.setStatus("failing");
                    providerHost.setUrl("service:" + hostRpcPort + "-" + service + "-" + version);
                    if (servicesFailing.get(serviceKey) == null) {
                        Set<GrpcHost> provider = Sets.newHashSet(providerHost);
                        providerAndConsumer = new ImmutablePair<Set<GrpcHost>, Set<GrpcHost>>(provider, null);
                        servicesFailing.put(serviceKey, providerAndConsumer);
                    }
                    providerAndConsumer.getLeft().add(providerHost);
                }
            }
        }
    }
    public Map<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> getAllPassingService() {
        return this.servicesPassing;
    }
    public Map<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> getAllFailingService() {
        return this.servicesFailing;
    }
    private Pair<Set<GrpcHost>, Set<GrpcHost>> getProviderAndConsumer(String group, String service, String version) {
        Set<GrpcHost> providerHosts = Sets.newHashSet();
        Set<GrpcHost> comsumerHosts = Sets.newHashSet();
        List<String> providerAndConsumerKvs = consulClient.getKVKeysOnly(group + "/" + service + "/"
                                                                         + version).getValue();
        if (providerAndConsumerKvs != null) {
            for (String providerAndConsumerKv : providerAndConsumerKvs) {
                Triple<String, String, String> machineInfo = getmachineInfo(providerAndConsumerKv,
                                                                            group + "/" + service);
                String appFlag = machineInfo.getLeft();
                String[] appHostRpcPort = machineInfo.getMiddle().split(":");
                String appHttpPort = machineInfo.getRight();
                if (appFlag.equals(Constants.PROVIDERS_CATEGORY)) {
                    GrpcHost host = new GrpcHost(appHostRpcPort[0], appHttpPort, appHostRpcPort[1]);
                    host.setStatus("passing");
                    host.setUrl("service:" + appHostRpcPort[0] + ":" + appHostRpcPort[1] + "-" + service + "-"
                                + version);
                    providerHosts.add(host);
                } else if (appFlag.equals(Constants.CONSUMERS_CATEGORY)) {
                    GrpcHost host = new GrpcHost(appHostRpcPort[0], appHttpPort, "0");
                    host.setStatus("passing");
                    host.setUrl(null);
                    comsumerHosts.add(host);
                }
            }
            return new ImmutablePair<Set<GrpcHost>, Set<GrpcHost>>(providerHosts, comsumerHosts);
        }
        return null;
    }
    /**
     * ==============help method=============
     */
    private Triple<String, String, String> getmachineInfo(String providerAndConsumerKv, String groupService) {
        String thralUrl = consulClient.getKVValue(providerAndConsumerKv).getValue().getDecodedValue();
        GrpcURL url = GrpcURL.valueOf(thralUrl);
        String flagAndIp = StringUtils.remove(providerAndConsumerKv, groupService + "/");
        String[] serverInfos = StringUtils.split(flagAndIp, "/");
        String machineFlag = serverInfos[1];
        return new ImmutableTriple<String, String, String>(machineFlag, url.getAddress(),
                                                           url.getParameter(Constants.HTTP_PORT_KEY));
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
        return StringUtils.remove(group, Constants.CONSUL_SERVICE_PRE) + ":" + service + ":" + version;
    }
    public String getAgentHost() {
        return agentHost;
    }
    public void setAgentHost(String agentHost) {
        this.agentHost = agentHost;
    }
}