package com.quancheng.saluki.monitor.web;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Check;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TestMain {

    public static final String                             CONSUL_SERVICE_PRE        = "Saluki_";

    private static final Map<String, Pair<String, String>> serviceApplicationMapping = Maps.newConcurrentMap();

    private static final Map<String, Set<String>>          applicationHostsMapping   = Maps.newConcurrentMap();

    private ConsulClient                                   consulClient              = new ConsulClient("192.168.99.101",
                                                                                                        8500);
    private final Gson                                     gson                      = new Gson();

    public static void main(String[] args) {
        TestMain main = new TestMain();
        main.getAllApplication();

        System.out.println(applicationHostsMapping);
    }

    public void getAllApplication() {
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
            String[] serverInfos = StringUtils.split(StringUtils.remove(providerAndConsumer, serviceKey + "/"), "/");
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
            // logger.error(serviceKey + " have not registry appName");
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
