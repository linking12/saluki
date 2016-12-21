/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.boot.runner;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Service;

import com.quancheng.saluki.core.config.RpcServiceConfig;
import com.quancheng.saluki.boot.SalukiService;
import com.quancheng.saluki.boot.autoconfigure.ThrallProperties;

/**
 * @author shimingliu 2016年12月16日 下午5:07:16
 * @version ThrallServiceRunner.java, v 0.0.1 2016年12月16日 下午5:07:16 shimingliu
 */
public class ThrallServiceRunner implements DisposableBean, CommandLineRunner {

    private static final Logger        log = LoggerFactory.getLogger(ThrallServiceRunner.class);

    private final ThrallProperties     thrallProperties;

    @Value("${spring.application.name}")
    private String                     applicationName;

    @Autowired
    private AbstractApplicationContext applicationContext;

    private RpcServiceConfig           rpcService;

    public ThrallServiceRunner(ThrallProperties thrallProperties){
        this.thrallProperties = thrallProperties;
    }

    @Override
    public void destroy() throws Exception {
        rpcService.destroy();
        applicationContext.destroy();
    }

    @Override
    public void run(String... arg0) throws Exception {
        log.info("Starting GRPC Server ...");
        RpcServiceConfig rpcSerivceConfig = new RpcServiceConfig();
        this.addRegistyAddress(rpcSerivceConfig);
        rpcSerivceConfig.setApplication(applicationName);
        this.addHostAndPort(rpcSerivceConfig);
        rpcSerivceConfig.setMonitorinterval(thrallProperties.getMonitorinterval());
        for (Object instance : getTypedBeansWithAnnotation(SalukiService.class)) {
            SalukiService serviceAnnotation = instance.getClass().getAnnotation(SalukiService.class);
            String serviceName = serviceAnnotation.service();
            if (StringUtils.isBlank(serviceName)) {
                if (this.isGrpcServer(instance)) {
                    throw new java.lang.IllegalArgumentException("you use grpc stub service,must set service name,service instance is"
                                                                 + instance);
                } else {
                    serviceName = instance.getClass().getInterfaces()[0].getName();
                }
            }
            rpcSerivceConfig.addServiceDefinition(serviceName, getGroup(serviceAnnotation),
                                                  getVersion(serviceAnnotation), instance);
        }
        this.rpcService = rpcSerivceConfig;
        rpcSerivceConfig.export();
    }

    private void addHostAndPort(RpcServiceConfig rpcSerivceConfig) {
        rpcSerivceConfig.setRealityRpcPort(getRealityRpcPort());
        rpcSerivceConfig.setRegistryRpcPort(thrallProperties.getRegistryRpcPort());
        rpcSerivceConfig.setHost(thrallProperties.getHost());
        rpcSerivceConfig.setHttpPort(thrallProperties.getRegistryHttpPort());
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

    private String getGroup(SalukiService service) {
        if (StringUtils.isNoneBlank(service.group())) {
            return service.group();
        } else {
            String group = thrallProperties.getGroup();
            if (StringUtils.isBlank(group)) {
                throw new java.lang.IllegalArgumentException("group can not be null or empty");
            }
            return group;
        }
    }

    private String getVersion(SalukiService service) {
        if (StringUtils.isNoneBlank(service.version())) {
            return service.version();
        } else {
            String version = thrallProperties.getVersion();
            if (StringUtils.isBlank(version)) {
                throw new java.lang.IllegalArgumentException("version can not be null or empty");
            }
            return version;
        }
    }

    private boolean isGrpcServer(Object instance) {
        if (instance instanceof io.grpc.BindableService) {
            return true;
        } else {
            return false;
        }
    }

    private Collection<Object> getTypedBeansWithAnnotation(Class<? extends Annotation> annotationType) throws Exception {
        return Stream.of(applicationContext.getBeanNamesForAnnotation(Service.class)).filter(name -> {
            BeanDefinition beanDefinition = applicationContext.getBeanFactory().getBeanDefinition(name);
            if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
                StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                return metadata.isAnnotated(annotationType.getName());
            }
            return null != applicationContext.getBeanFactory().findAnnotationOnBean(name, annotationType);
        }).map(name -> applicationContext.getBeanFactory().getBean(name)).collect(Collectors.toList());

    }

}
