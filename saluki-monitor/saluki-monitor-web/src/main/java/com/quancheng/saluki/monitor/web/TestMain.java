package com.quancheng.saluki.monitor.web;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

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

public class TestMain {

    public static final String                                       CONSUL_SERVICE_PRE          = "Saluki_";

    private static final Map<String, Triple<String, String, String>> servicesPassingApplications = Maps.newConcurrentMap();

    private static final Map<String, Triple<String, String, String>> servicesFailingApplications = Maps.newConcurrentMap();

    private static final Map<String, Set<SalukiHost>>                applicationPassingHost      = Maps.newConcurrentMap();

    private static final Map<String, Set<SalukiHost>>                applicationFailingHost      = Maps.newConcurrentMap();

    private ConsulClient                                             consulClient                = new ConsulClient("192.168.99.101",
                                                                                                                    8500);
    private final Gson                                               gson                        = new Gson();

    public static void main(String[] args) {
        TestMain main = new TestMain();
        main.getAllApplication1212();
        List<SalukiApplication> apps = main.getAllApplication();
        System.out.println(new Gson().toJson(apps));
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

    public void getAllApplication1212() {
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
            String[] serverInfos = StringUtils.split(StringUtils.remove(providerAndConsumer, serviceKey + "/"), "/");
            String appFlag = serverInfos[0];
            String appHost = serverInfos[1];
            String appHttpPort = appInfoMap.get("serverHttpPort");
            // 对于provider端，直接取group做为应用名
            if (appFlag.equals("provider")) {
                String appName = StringUtils.replace(group, CONSUL_SERVICE_PRE, "");
                providerAppName = appName;
                String[] rpcPort = StringUtils.split(serviceRpcPort, ":");
                providerHosts.add(new SalukiHost(appHost, appHttpPort, rpcPort.length > 1 ? rpcPort[1] : rpcPort[0]));
            } // 对于consumer端，需要取注册的参数做为应用名
            else if (appFlag.equals("consumer")) {
                String appName = appInfoMap.get("appName");
                consumerAppName = appName;
                comsumerHosts.add(new SalukiHost(appHost, appHttpPort, "0"));
            }
        }
        if (providerAppName == null || consumerAppName == null) {
            // logger.error(serviceKey + " have not registry appName");
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
