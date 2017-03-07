/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.boot.runner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.Environment;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.boot.autoconfigure.GrpcProperties;
import com.quancheng.saluki.core.config.RpcReferenceConfig;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.core.utils.CollectionUtils;

import io.grpc.stub.AbstractStub;

/**
 * @author shimingliu 2016年12月16日 下午5:07:02
 * @version ThrallReferenceRunner.java, v 0.0.1 2016年12月16日 下午5:07:02 shimingliu
 */
public class GrpcReferenceRunner extends InstantiationAwareBeanPostProcessorAdapter {

    private static final Logger             logger                     = LoggerFactory.getLogger(GrpcReferenceRunner.class);

    private static final Pattern            REPLACE_PATTERN            = Pattern.compile("#\\{(.*?)\\}");

    private final List<Map<String, String>> servcieReferenceDefintions = Lists.newArrayList();

    private final GrpcProperties            thrallProperties;

    @Value("${spring.application.name}")
    private String                          applicationName;

    @Value("${server.port}")
    private int                             httpPort;

    @Autowired
    private AbstractApplicationContext      applicationContext;

    @Autowired
    private Environment                     env;

    public GrpcReferenceRunner(GrpcProperties thrallProperties){
        this.thrallProperties = thrallProperties;
        String referenceDefinPath = thrallProperties.getReferenceDefinition();
        if (StringUtils.isNoneBlank(referenceDefinPath)) {
            InputStream in = GrpcReferenceRunner.class.getClassLoader().getResourceAsStream(referenceDefinPath);
            servcieReferenceDefintions.addAll(new Gson().fromJson(new InputStreamReader(in),
                                                                  new TypeToken<List<Map<String, String>>>() {
                                                                  }.getType()));
        } else {
            logger.warn("Waring! there is no reference config in classpath,You must config in annoation");
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> searchType = bean.getClass();
        while (!Object.class.equals(searchType) && searchType != null) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                SalukiReference reference = field.getAnnotation(SalukiReference.class);
                if (reference != null) {
                    Object value = null;
                    try {
                        value = applicationContext.getBean(field.getType());
                    } catch (NoSuchBeanDefinitionException e) {
                        value = null;
                    }
                    if (value == null) {
                        value = refer(reference, field.getType());
                    }
                    try {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        field.set(bean, value);
                    } catch (Throwable e) {
                        logger.error("Failed to init remote service reference at filed " + field.getName()
                                     + " in class " + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
                    }
                }
            }
            searchType = searchType.getSuperclass();
        }
        return bean;
    }

    private Object refer(SalukiReference reference, Class<?> referenceClass) {
        RpcReferenceConfig rpcReferenceConfig = new RpcReferenceConfig();
        String serviceName = this.getServiceName(reference, referenceClass);
        rpcReferenceConfig.setServiceName(serviceName);
        rpcReferenceConfig.setApplication(applicationName);
        String group = this.getGroup(reference, serviceName, referenceClass);
        rpcReferenceConfig.setGroup(group);
        String version = this.getVersion(reference, serviceName, referenceClass);
        rpcReferenceConfig.setVersion(version);
        this.addHaRetries(reference, rpcReferenceConfig);
        this.addRegistyAddress(rpcReferenceConfig);
        this.addAsyncAndTimeOut(reference, rpcReferenceConfig);
        this.addMonitorInterval(rpcReferenceConfig);
        this.addHostAndPort(rpcReferenceConfig);
        if (this.isGenericClient(referenceClass)) {
            rpcReferenceConfig.setGeneric(true);
        }
        if (this.isGrpcStubClient(referenceClass)) {
            rpcReferenceConfig.setGrpcStub(true);
            rpcReferenceConfig.setServiceClass(referenceClass);
        }
        return rpcReferenceConfig.getProxyObj();
    }

    private void addHostAndPort(RpcReferenceConfig rpcReferenceConfig) {
        String host = thrallProperties.getHost();
        int registryHttpPort = thrallProperties.getRegistryHttpPort();
        if (StringUtils.isNoneBlank(host)) {
            rpcReferenceConfig.setHost(host);
        }
        if (registryHttpPort == 0) {
            if (this.httpPort != 0) {
                rpcReferenceConfig.setHttpPort(this.httpPort);
                thrallProperties.setRegistryHttpPort(this.httpPort);
            } else {
                throw new java.lang.IllegalArgumentException("http port must be set in properties");
            }
        } else {
            rpcReferenceConfig.setHttpPort(registryHttpPort);
        }

    }

    private void addMonitorInterval(RpcReferenceConfig rpcReferenceConfig) {
        if (thrallProperties.getMonitorinterval() != 0) {
            rpcReferenceConfig.setMonitorinterval(thrallProperties.getMonitorinterval());
        }
    }

    private void addAsyncAndTimeOut(SalukiReference reference, RpcReferenceConfig rpcReferenceConfig) {
        rpcReferenceConfig.setAsync(reference.async());
        if (reference.timeOut() != 0) {
            rpcReferenceConfig.setTimeout(reference.timeOut());
        }
    }

    private void addRegistyAddress(RpcReferenceConfig rpcReferenceConfig) {
        String registryAddress = thrallProperties.getRegistryAddress();
        if (StringUtils.isBlank(registryAddress)) {
            throw new java.lang.IllegalArgumentException("registry address can not be null or empty");
        } else {
            String[] registryHostAndPort = StringUtils.split(registryAddress, ":");
            if (registryHostAndPort.length < 2) {
                throw new java.lang.IllegalArgumentException("the pattern of registry address is host:port");
            }
            rpcReferenceConfig.setRegistryAddress(registryHostAndPort[0]);
            rpcReferenceConfig.setRegistryPort(Integer.valueOf(registryHostAndPort[1]));
        }
    }

    private void addHaRetries(SalukiReference reference, RpcReferenceConfig rpcReferenceConfig) {
        if (reference.retries() > 1) {
            if (CollectionUtils.isEmpty(reference.retryMethods())) {
                logger.warn("Have set retries,but not have set method,will set all method to retry");
                rpcReferenceConfig.setRetryMethods(new HashSet<String>(Arrays.asList(reference.retryMethods())));
                rpcReferenceConfig.setReties(reference.retries());
            }
        }
    }

    private String getServiceName(SalukiReference reference, Class<?> referenceClass) {
        String serviceName = reference.service();
        if (StringUtils.isBlank(serviceName)) {
            if (this.isGrpcStubClient(referenceClass)) {
                throw new java.lang.IllegalArgumentException("reference service can not be null or empty");
            } else {
                serviceName = referenceClass.getName();
            }
        }
        return serviceName;
    }

    private String getGroup(SalukiReference reference, String serviceName, Class<?> referenceClass) {
        Pair<String, String> groupVersion = findGroupAndVersionByServiceName(serviceName);
        if (StringUtils.isNoneBlank(reference.group())) {
            return reference.group();
        } else if (StringUtils.isNoneBlank(groupVersion.getLeft())) {
            String replaceGroup = groupVersion.getLeft();
            Matcher matcher = REPLACE_PATTERN.matcher(replaceGroup);
            if (matcher.find()) {
                String replace = matcher.group().substring(2, matcher.group().length() - 1).trim();
                String[] replaces = StringUtils.split(replace, ":");
                if (replaces.length == 2) {
                    String realGroup = env.getProperty(replaces[0], replaces[1]);
                    return realGroup;
                } else {
                    throw new IllegalArgumentException("replaces formater is #{XXXgroup:groupName}");
                }
            } else {
                return replaceGroup;
            }
        } else if (this.isGenericClient(referenceClass)) {
            return StringUtils.EMPTY;
        }
        throw new java.lang.IllegalArgumentException(String.format("reference group can not be null or empty,the servicName is %s",
                                                                   serviceName));

    }

    private String getVersion(SalukiReference reference, String serviceName, Class<?> referenceClass) {
        Pair<String, String> groupVersion = findGroupAndVersionByServiceName(serviceName);
        if (StringUtils.isNoneBlank(reference.version())) {
            return reference.version();
        } else if (StringUtils.isNoneBlank(groupVersion.getRight())) {
            String replaceVersion = groupVersion.getRight();
            Matcher matcher = REPLACE_PATTERN.matcher(replaceVersion);
            if (matcher.find()) {
                String replace = matcher.group().substring(2, matcher.group().length() - 1).trim();
                String[] replaces = StringUtils.split(replace, ":");
                if (replaces.length == 2) {
                    String realVersion = env.getProperty(replaces[0], replaces[1]);
                    return realVersion;
                } else {
                    throw new IllegalArgumentException("replaces formater is #{XXXservice:1.0.0}");
                }
            } else {
                return replaceVersion;
            }
        } else if (this.isGenericClient(referenceClass)) {
            return StringUtils.EMPTY;
        } else {
            throw new java.lang.IllegalArgumentException("reference version can not be null or empty");
        }
    }

    private Pair<String, String> findGroupAndVersionByServiceName(String serviceName) {
        for (Map<String, String> referenceDefintion : servcieReferenceDefintions) {
            String servcieDefineName = referenceDefintion.get("service");
            if (servcieDefineName.equals(serviceName)) {
                String group = referenceDefintion.get("group");
                String version = referenceDefintion.get("version");
                return new ImmutablePair<String, String>(group, version);
            }
        }
        return new ImmutablePair<String, String>(null, null);
    }

    private boolean isGrpcStubClient(Class<?> referenceClass) {
        if (AbstractStub.class.isAssignableFrom(referenceClass)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isGenericClient(Class<?> referenceClass) {
        if (GenericService.class.isAssignableFrom(referenceClass)) {
            return true;
        } else {
            return false;
        }
    }

}
