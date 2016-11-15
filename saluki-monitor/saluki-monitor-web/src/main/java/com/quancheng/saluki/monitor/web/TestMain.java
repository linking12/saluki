package com.quancheng.saluki.monitor.web;

import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Check;
import com.google.common.collect.Lists;
import com.quancheng.saluki.monitor.domain.SalukiApplication;

public class TestMain {

    public static void main(String[] args) {
        TestMain main = new TestMain();
        main.getAllApplication();
    }

    public List<SalukiApplication> getAllApplication() {
        ConsulClient consulClient = new ConsulClient("192.168.99.101", 8500);
        List<SalukiApplication> applications = Lists.newArrayList();
        Map<String, Check> allServices = consulClient.getAgentChecks().getValue();
        for (Map.Entry<String, Check> entry : allServices.entrySet()) {
            Check serviceCheck = entry.getValue();
            if (serviceCheck.getStatus() == Check.CheckStatus.PASSING) {
                String group = serviceCheck.getServiceName();
                String[] args = serviceCheck.getServiceId().split("-");
                String serviceName = args[1];
                List<String> appInfoKvs = consulClient.getKVKeysOnly(group + "/" + serviceName).getValue();
                for (String consumer_host_kv : appInfoKvs) {

                }
            }
        }

        return applications;
    }

}
