package com.quancheng.saluki.monitor.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.quancheng.saluki.domain.Application;
import com.quancheng.saluki.domain.GrpcHost;
import com.quancheng.saluki.domain.GrpcService;
import com.quancheng.saluki.monitor.repository.ConsulRegistryRepository;

@Service
public class ConsulRegistryService {

    private static final Logger      log = LoggerFactory.getLogger(ConsulRegistryService.class);

    @Autowired
    private ConsulRegistryRepository registryRepository;

    /**
     * 获取所有应用
     */
    public List<Application> getAllApplication() {
        Map<String, Application> appCache = Maps.newHashMap();
        registryRepository.loadAllServiceFromConsul();
        Map<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> servicesPassing = registryRepository.getAllPassingService();
        Map<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> servicesFailing = registryRepository.getAllFailingService();
        for (Map.Entry<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> entry : servicesPassing.entrySet()) {
            processApplication(appCache, entry);
        }
        for (Map.Entry<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> entry : servicesFailing.entrySet()) {
            processApplication(appCache, entry);
        }
        List<Application> applications = Lists.newArrayList();
        for (Map.Entry<String, Application> entry : appCache.entrySet()) {
            applications.add(entry.getValue());
        }
        return applications;
    }

    private void processApplication(Map<String, Application> appCache,
                                    Map.Entry<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> entry) {
        Triple<String, String, String> appNameServiceVersion = getAppNameServiceVersion(entry.getKey());
        Pair<Set<GrpcHost>, Set<GrpcHost>> providerConsumer = entry.getValue();
        String appName = appNameServiceVersion.getLeft();
        String serviceName = appNameServiceVersion.getMiddle();
        String version = appNameServiceVersion.getRight();
        Application application = new Application(appName);
        GrpcService service = new GrpcService(appName, version, serviceName);
        if (providerConsumer.getLeft() != null) {
            service.addProviderHosts(providerConsumer.getLeft());
        }
        if (providerConsumer.getRight() != null) {
            service.addConsumerHosts(providerConsumer.getRight());
        }
        application.addService(service);
        if (appCache.get(application.getAppName()) == null) {
            appCache.put(application.getAppName(), application);
        } else {
            appCache.get(application.getAppName()).addServices(application.getServices());
        }
    }

    /**
     * 匹配服务，从服务角度查询
     */
    public List<GrpcService> queryPassingServiceByService(String search, Boolean accurate) {
        Set<String> beAboutToQuery = buildQueryCondition(search, "service", accurate);
        if (beAboutToQuery.size() == 0) {
            log.debug("fuzzy query response null,there is no data in cache,will load service into cache ");
            registryRepository.loadAllServiceFromConsul();
            beAboutToQuery = buildQueryCondition(search, "service", accurate);

        }
        return buildQueryResponse(beAboutToQuery, accurate);
    }

    /**
     * 匹配服务，从应用角度查询
     */
    public List<GrpcService> queryPassingServiceByApp(String search, Boolean accurate) {
        Set<String> beAboutToQuery = buildQueryCondition(search, "application", accurate);
        if (beAboutToQuery.size() == 0) {
            log.debug("fuzzy query response null,there is no data in cache,will load service into cache ");
            registryRepository.loadAllServiceFromConsul();
            beAboutToQuery = buildQueryCondition(search, "application", accurate);

        }
        return buildQueryResponse(beAboutToQuery, accurate);
    }

    private Set<String> buildQueryCondition(String search, String dimension, Boolean accurate) {
        Set<String> beAboutToQuery = Sets.newHashSet();
        Map<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> servicesPassing = registryRepository.getAllPassingService();
        for (Map.Entry<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> entry : servicesPassing.entrySet()) {
            String serviceKey = entry.getKey();
            Triple<String, String, String> appNameServiceVersion = getAppNameServiceVersion(serviceKey);
            String appName = appNameServiceVersion.getLeft();
            String serviceName = appNameServiceVersion.getMiddle();
            if (dimension.equals("service")) {
                if (accurate) {
                    if (StringUtils.equalsIgnoreCase(serviceName, search)) {
                        beAboutToQuery.add(serviceKey);
                    }
                } else {
                    if (StringUtils.containsIgnoreCase(serviceName, search)) {
                        beAboutToQuery.add(serviceKey);
                    }
                }
            } else {
                if (accurate) {
                    if (StringUtils.equalsIgnoreCase(appName, search)) {
                        beAboutToQuery.add(serviceKey);
                    }
                } else {
                    if (StringUtils.containsIgnoreCase(appName, search)) {
                        beAboutToQuery.add(serviceKey);
                    }
                }
            }
        }
        return beAboutToQuery;
    }

    private List<GrpcService> buildQueryResponse(Set<String> queryCondition, Boolean accurate) {
        List<GrpcService> services = Lists.newArrayList();
        Map<String, Pair<Set<GrpcHost>, Set<GrpcHost>>> servicesPassing = registryRepository.getAllPassingService();
        for (Iterator<String> it = queryCondition.iterator(); it.hasNext();) {
            String serviceKey = it.next();
            Triple<String, String, String> appNameServiceVersion = getAppNameServiceVersion(serviceKey);
            Pair<Set<GrpcHost>, Set<GrpcHost>> providerConsumer = servicesPassing.get(serviceKey);
            GrpcService service = new GrpcService(appNameServiceVersion.getLeft(), appNameServiceVersion.getRight(),
                                                  appNameServiceVersion.getMiddle());
            service.setProviderHost(providerConsumer.getLeft());
            service.setConsumerHost(providerConsumer.getRight());
            services.add(service);
        }
        return services;
    }

    /**
     * ==============help method=============
     */
    private Triple<String, String, String> getAppNameServiceVersion(String serviceKey) {
        String[] args = serviceKey.split(":");
        return new ImmutableTriple<String, String, String>(args[0], args[1], args[2]);
    }

}
