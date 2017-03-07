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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.ClassUtils;

import com.google.common.collect.Sets;
import com.quancheng.saluki.boot.SalukiService;
import com.quancheng.saluki.boot.autoconfigure.GrpcProperties;
import com.quancheng.saluki.boot.common.GrpcAopUtils;
import com.quancheng.saluki.boot.service.HealthImpl;
import com.quancheng.saluki.core.config.RpcServiceConfig;
import com.quancheng.saluki.service.Health;

/**
 * @author shimingliu 2016年12月16日 下午5:07:16
 * @version ThrallServiceRunner.java, v 0.0.1 2016年12月16日 下午5:07:16 shimingliu
 */
public class GrpcServiceRunner implements DisposableBean, CommandLineRunner {

    private final GrpcProperties       thrallProperties;

    @Value("${spring.application.name}")
    private String                     applicationName;

    @Value("${server.port}")
    private int                        httpPort;

    @Autowired
    private AbstractApplicationContext applicationContext;

    private RpcServiceConfig           rpcService;

    public GrpcServiceRunner(GrpcProperties thrallProperties){
        this.thrallProperties = thrallProperties;
    }

    @Override
    public void destroy() throws Exception {
        if (rpcService != null) {
            rpcService.destroy();
        }
        if (applicationContext != null) {
            applicationContext.destroy();
        }
    }

    @Override
    public void run(String... arg0) throws Exception {
        System.out.println("Starting GRPC Server ...");
        RpcServiceConfig rpcSerivceConfig = new RpcServiceConfig();
        this.addRegistyAddress(rpcSerivceConfig);
        rpcSerivceConfig.setApplication(applicationName);
        this.addHostAndPort(rpcSerivceConfig);
        rpcSerivceConfig.setMonitorinterval(thrallProperties.getMonitorinterval());
        Collection<Object> instances = getTypedBeansWithAnnotation(SalukiService.class);
        if (instances.size() > 0) {
            try {
                for (Object instance : instances) {
                    SalukiService serviceAnnotation = instance.getClass().getAnnotation(SalukiService.class);
                    String serviceName = serviceAnnotation.service();
                    Set<String> serviceNames = Sets.newHashSet();
                    Object target = instance;
                    if (StringUtils.isBlank(serviceName)) {
                        if (this.isGrpcServer(instance)) {
                            throw new java.lang.IllegalArgumentException("you use grpc stub service,must set service name,service instance is"
                                                                         + instance);
                        } else {
                            target = GrpcAopUtils.getTarget(target);
                            Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(target.getClass());
                            for (Class<?> interfaceClass : interfaces) {
                                String interfaceName = interfaceClass.getName();
                                if (StringUtils.startsWith(interfaceName, "com.quancheng")) {
                                    serviceNames.add(interfaceName);
                                }
                            }
                        }
                    } else {
                        serviceNames.add(serviceName);
                    }
                    for (String realServiceName : serviceNames) {
                        rpcSerivceConfig.addServiceDefinition(realServiceName, getGroup(serviceAnnotation),
                                                              getVersion(serviceAnnotation), instance);
                    }
                }
            } finally {
                Object healthInstance = new HealthImpl(applicationContext);
                BeanDefinitionRegistry beanDefinitonRegistry = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Health.class);
                beanDefinitonRegistry.registerBeanDefinition(Health.class.getName(),
                                                             beanDefinitionBuilder.getRawBeanDefinition());
                applicationContext.getBeanFactory().registerSingleton(Health.class.getName(), healthInstance);
                String group = thrallProperties.getGroup() != null ? thrallProperties.getGroup() : "default";
                String version = thrallProperties.getVersion() != null ? thrallProperties.getVersion() : "1.0.0";
                rpcSerivceConfig.addServiceDefinition(Health.class.getName(), group, version, healthInstance);
            }
        }
        this.rpcService = rpcSerivceConfig;
        rpcSerivceConfig.export();
        System.out.println(String.format("GRPC server has started!You can do test by %s \n %s",
                                         "http://localhost:" + httpPort + "/doc",
                                         "http://saluki.dev.quancheng-ec.com"));
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
        return Stream.of(applicationContext.getBeanNamesForAnnotation(annotationType)).filter(name -> {
            BeanDefinition beanDefinition = applicationContext.getBeanFactory().getBeanDefinition(name);
            if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
                StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                return metadata.isAnnotated(annotationType.getName());
            }
            return null != applicationContext.getBeanFactory().findAnnotationOnBean(name, annotationType);
        }).map(name -> applicationContext.getBeanFactory().getBean(name)).collect(Collectors.toList());

    }

}
