package com.quancheng.saluki.monitor.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.quancheng.saluki.monitor.SalukiAppDependcy;
import com.quancheng.saluki.monitor.repository.SalukiInvokeMapper;

@Service
public class SalukiAppDependcyService {

    @Autowired
    private SalukiInvokeMapper invokeMapper;

    public List<SalukiAppDependcy> queryApplicationDependcy() {
        List<Map<String, String>> allConsumers = invokeMapper.queryConsumer();
        List<SalukiAppDependcy> appDepencys = Lists.newArrayList();
        for (Map<String, String> consumer : allConsumers) {
            String appName = consumer.get("application");
            String providerHost = consumer.get("provider");
            String service = consumer.get("service");
            String callCount = String.valueOf(consumer.get("callCount"));
            SalukiAppDependcy hasAddedApp = null;
            for (SalukiAppDependcy salukiAppDependcy : appDepencys) {
                if (salukiAppDependcy.getAppName().equals(appName)) {
                    hasAddedApp = salukiAppDependcy;
                }
            }
            if (hasAddedApp == null) {
                hasAddedApp = new SalukiAppDependcy(appName);
                appDepencys.add(hasAddedApp);
            }
            Map<String, String> providerParam = Maps.newHashMap();
            providerParam.put("provider", providerHost);
            providerParam.put("service", service);
            String parentAppName = invokeMapper.queryProvider(providerParam).get("application");
            hasAddedApp.addDependcyService(parentAppName,
                                           new ImmutablePair<String, Integer>(service, Integer.valueOf(callCount)));
        }
        return appDepencys;
    }

}
