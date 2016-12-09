package com.quancheng.saluki.monitor.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.quancheng.saluki.monitor.SalukiAppDependcy;
import com.quancheng.saluki.monitor.repository.SalukiInvokeMapper;

@Service
public class SalukiApplicationDependcyService {

    @Autowired
    private SalukiInvokeMapper invokeMapper;

    private static final int   PAGE_SIZE = 5;

    public List<SalukiAppDependcy> queryApplicationDependcy(int pageNum) {
        Map<String, Integer> consumerParam = Maps.newHashMap();
        consumerParam.put("p1", getPageStart(pageNum));
        consumerParam.put("p2", PAGE_SIZE);
        List<Map<String, String>> allConsumers = invokeMapper.queryConsumer(consumerParam);
        Map<String, Set<String>> depencyApp = Maps.newHashMap();
        Map<String, Set<Pair<String, Integer>>> depencyService = Maps.newHashMap();
        for (Map<String, String> consumer : allConsumers) {
            // consumer
            String appName = consumer.get("application");
            String providerHost = consumer.get("provider");
            String service = consumer.get("service");
            String callCount = String.valueOf(consumer.get("callCount"));
            Set<Pair<String, Integer>> services = null;
            if (!depencyService.containsKey(appName)) {
                services = Sets.newHashSet();
                depencyService.put(appName, services);
            }
            depencyService.get(appName).add(new ImmutablePair<String, Integer>(service, Integer.valueOf(callCount)));
            // provider
            Map<String, String> providerParam = Maps.newHashMap();
            providerParam.put("provider", providerHost);
            providerParam.put("service", service);
            String providerAppName = invokeMapper.queryProvider(providerParam).get("application");
            Set<String> providerApps = null;
            if (!depencyApp.containsKey(appName)) {
                providerApps = Sets.newHashSet();
                depencyApp.put(appName, providerApps);
            }
            depencyApp.get(appName).add(providerAppName);
        }
        List<SalukiAppDependcy> appDepency = Lists.newArrayList();
        for (Map.Entry<String, Set<String>> entry : depencyApp.entrySet()) {
            SalukiAppDependcy salukiAppDependcy = new SalukiAppDependcy();
            String appName = entry.getKey();
            salukiAppDependcy.setAppName(appName);
            salukiAppDependcy.setDependcyApps(depencyApp.get(appName));
            salukiAppDependcy.addDependcyService(depencyService.get(appName));
            appDepency.add(salukiAppDependcy);
        }
        return appDepency;
    }

    private int getPageStart(int pageNum) {
        int pageStart = (pageNum - 1) * PAGE_SIZE;
        if (pageStart <= 0) {
            pageStart = 0;
        }
        return pageStart;
    }

}
