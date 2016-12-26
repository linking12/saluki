/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.config;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;

/**
 * @author shimingliu 2016年12月14日 下午2:14:34
 * @version RpcServiceConfig.java, v 0.0.1 2016年12月14日 下午2:14:34 shimingliu
 */
public class RpcServiceConfig extends RpcBaseConfig {

    private static final long                         serialVersionUID     = 2638920613685526606L;

    private final Set<RpcServiceSingleConfig<Object>> singleServiceConfigs = Sets.newHashSet();

    private transient io.grpc.Server                  internalServer;

    public void destroy() {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                internalServer.shutdown();
            }
        });
    }

    public void addServiceDefinition(String serviceName, String group, String version, Object instance) {
        RpcServiceSingleConfig<Object> singleServiceConfig = new RpcServiceSingleConfig<Object>();
        singleServiceConfig.setGroup(group);
        singleServiceConfig.setVersion(version);
        singleServiceConfig.setServiceName(serviceName);
        singleServiceConfig.setRef(instance);
        singleServiceConfigs.add(singleServiceConfig);
    }

    public synchronized void export() {
        Map<GrpcURL, Object> providerUrls = Maps.newHashMap();
        for (RpcServiceSingleConfig<Object> singleServiceConfig : singleServiceConfigs) {
            String serviceName = singleServiceConfig.getServiceName();
            Object serviceRef = singleServiceConfig.getRef();
            Map<String, String> params = Maps.newHashMap();
            this.addGroup(singleServiceConfig, params);
            this.addVersion(singleServiceConfig, params);
            this.addApplication(params);
            this.addInterval(params);
            this.addRegistryRpcPort(params);
            this.addHttpPort(params);
            GrpcURL providerUrl = new GrpcURL(Constants.REMOTE_PROTOCOL, super.getHost(), super.getRealityRpcPort(),
                                                  serviceName, params);
            providerUrls.put(providerUrl, serviceRef);
        }
        try {
            internalServer = super.getGrpcEngine().getServer(providerUrls, super.getRealityRpcPort());
            Thread awaitThread = new Thread() {

                @Override
                public void run() {
                    try {
                        internalServer.start();
                        internalServer.awaitTermination();
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    } catch (IOException e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                }

            };
            awaitThread.setDaemon(false);
            awaitThread.start();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }

    private void addGroup(RpcServiceSingleConfig<Object> singleConfig, Map<String, String> params) {
        String group = singleConfig.getGroup();
        if (StringUtils.isNotBlank(group)) {
            params.put(Constants.GROUP_KEY, group);
        } else {
            String application = super.getApplication();
            if (StringUtils.isNotBlank(application)) {
                params.put(Constants.GROUP_KEY, application);
            }
        }
    }

    private void addVersion(RpcServiceSingleConfig<Object> singleConfig, Map<String, String> params) {
        String version = singleConfig.getVersion();
        if (StringUtils.isNotBlank(version)) {
            params.put(Constants.VERSION_KEY, version);
        }
    }

    private void addApplication(Map<String, String> params) {
        String application = super.getApplication();
        if (StringUtils.isNotBlank(application)) {
            params.put(Constants.APPLICATION_NAME, application);
        }
    }

    private void addInterval(Map<String, String> params) {
        Integer interval = getMonitorinterval();
        if (interval != 0) {
            params.put(Constants.MONITOR_INTERVAL, interval.toString());
        }
    }

    private void addRegistryRpcPort(Map<String, String> params) {
        Integer registryRpcPort = super.getRegistryRpcPort();
        if (registryRpcPort != 0) {
            params.put(Constants.REGISTRY_RPC_PORT_KEY, registryRpcPort.toString());
        }
    }

}
