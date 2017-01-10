package com.quancheng.saluki.monitor.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.quancheng.saluki.domain.ApplicationDependcy;
import com.quancheng.saluki.monitor.repository.InvokeMapper;

@Service
public class ApplicationDependcyService {

    @Autowired
    private InvokeMapper invokeMapper;

    public List<ApplicationDependcy> queryApplicationDependcy() {
        List<Map<String, String>> allConsumers = invokeMapper.queryConsumer();
        List<ApplicationDependcy> appDepencys = Lists.newArrayList();
        for (Map<String, String> consumer : allConsumers) {
            String appName = consumer.get("application");
            String service = consumer.get("service");
            String callCount = String.valueOf(consumer.get("callCount"));
            ApplicationDependcy hasAddedApp = null;
            for (ApplicationDependcy ApplicationDependcy : appDepencys) {
                if (ApplicationDependcy.getAppName().equals(appName)) {
                    hasAddedApp = ApplicationDependcy;
                }
            }
            if (hasAddedApp == null) {
                hasAddedApp = new ApplicationDependcy(appName);
                appDepencys.add(hasAddedApp);
            }
            Map<String, String> providerParam = Maps.newHashMap();
            providerParam.put("service", service);
            Map<String, String> providerAppMap = invokeMapper.queryProvider(providerParam);
            String parentAppName = null;
            if (providerAppMap != null) {
                parentAppName = providerAppMap.get("application");
            } else {
                parentAppName = "No monitor provider data";
            }
            hasAddedApp.addDependcyService(parentAppName,
                                           new ImmutablePair<String, Integer>(service, Integer.valueOf(callCount)));
        }
        return appDepencys;
    }

}
