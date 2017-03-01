/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.springsupport.internal;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.quancheng.saluki.core.config.RpcServiceConfig;
import com.quancheng.saluki.core.config.RpcServiceSingleConfig;

/**
 * @author shimingliu 2017年3月1日 上午10:31:48
 * @version SingleServiceBean.java, v 0.0.1 2017年3月1日 上午10:31:48 shimingliu
 */
@SuppressWarnings("rawtypes")
public class ServiceBean extends RpcServiceSingleConfig implements DisposableBean, ApplicationListener<ContextRefreshedEvent> {

    private static final long       serialVersionUID = 1L;

    private static volatile boolean initialled       = false;

    private static final Object     lock             = new Object();

    private ConfigBean              thrallProperties;

    private RpcServiceConfig        rpcService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        synchronized (lock) {
            if (initialled) {
                return;
            } else {
                ApplicationContext applicationContext = event.getApplicationContext();
                Map<String, ServiceBean> instances = applicationContext.getBeansOfType(ServiceBean.class);
                if (!instances.isEmpty()) {
                    ConfigBean configBean = applicationContext.getBean(ConfigBean.class);
                    this.thrallProperties = configBean;
                    RpcServiceConfig rpcSerivceConfig = new RpcServiceConfig();
                    this.addRegistyAddress(rpcSerivceConfig);
                    rpcSerivceConfig.setApplication(thrallProperties.getApplication());
                    this.addHostAndPort(rpcSerivceConfig);
                    rpcSerivceConfig.setMonitorinterval(thrallProperties.getMonitorinterval());
                    for (Map.Entry<String, ServiceBean> entry : instances.entrySet()) {
                        ServiceBean serviceBean = entry.getValue();
                        rpcSerivceConfig.addServiceDefinition(serviceBean.getServiceName(), serviceBean.getGroup(),
                                                              serviceBean.getVersion(), serviceBean.getRef());
                    }
                    this.rpcService = rpcSerivceConfig;
                    initialled = true;
                    rpcSerivceConfig.export();
                }
            }

        }

    }

    @Override
    public void destroy() throws Exception {
        if (rpcService != null) {
            rpcService.destroy();
        }
    }

    private void addHostAndPort(RpcServiceConfig rpcSerivceConfig) {
        rpcSerivceConfig.setRealityRpcPort(getRealityRpcPort());
        rpcSerivceConfig.setRegistryRpcPort(thrallProperties.getRegistryRpcPort());
        rpcSerivceConfig.setHost(thrallProperties.getHost());
        rpcSerivceConfig.setHttpPort(thrallProperties.getHttpPort());
    }

    private void addRegistyAddress(RpcServiceConfig rpcSerivceConfig) {
        String registryAddress = thrallProperties.getRegistryAddress();
        if (StringUtils.isBlank(registryAddress)) {
            throw new java.lang.IllegalArgumentException("registry address can not be null or empty");
        } else {
            String[] registryHostAndPort = StringUtils.split(registryAddress, ":");
            if (registryHostAndPort.length < 2) {
                throw new java.lang.IllegalArgumentException("the pattern of registry address is host:port");
            }
            rpcSerivceConfig.setRegistryAddress(registryHostAndPort[0]);
            rpcSerivceConfig.setRegistryPort(Integer.valueOf(registryHostAndPort[1]));
        }
    }

    private int getRealityRpcPort() {
        int rpcPort = thrallProperties.getRealityRpcPort();
        if (rpcPort == 0) {
            throw new java.lang.IllegalArgumentException("rpcPort can not be null or empty");
        }
        return rpcPort;
    }

}
