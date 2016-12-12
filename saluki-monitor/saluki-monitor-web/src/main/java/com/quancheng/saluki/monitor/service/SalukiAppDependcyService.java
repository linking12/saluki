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

    private static final int   PAGE_SIZE = 5;

    public List<SalukiAppDependcy> queryApplicationDependcy(int pageNum) {
        Map<String, Integer> consumerParam = Maps.newHashMap();
        consumerParam.put("p1", getPageStart(pageNum));
        consumerParam.put("p2", PAGE_SIZE);
        List<Map<String, String>> allConsumers = invokeMapper.queryConsumer(consumerParam);
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

    private int getPageStart(int pageNum) {
        int pageStart = (pageNum - 1) * PAGE_SIZE;
        if (pageStart <= 0) {
            pageStart = 0;
        }
        return pageStart;
    }

}
