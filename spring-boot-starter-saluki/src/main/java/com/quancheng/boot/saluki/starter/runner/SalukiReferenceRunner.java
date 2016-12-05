package com.quancheng.boot.saluki.starter.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.support.AbstractApplicationContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.boot.saluki.starter.autoconfigure.SalukiProperties;
import com.quancheng.saluki.core.config.ReferenceConfig;
import com.quancheng.saluki.core.grpc.service.GenericService;

import io.grpc.stub.AbstractStub;

public class SalukiReferenceRunner extends InstantiationAwareBeanPostProcessorAdapter {

    private static final Logger        logger = LoggerFactory.getLogger(SalukiReferenceRunner.class);

    private final SalukiProperties     grpcProperties;

    @Autowired
    private AbstractApplicationContext applicationContext;

    private List<Map<String, String>>  servcieReferenceDefintions;

    public SalukiReferenceRunner(SalukiProperties grpcProperties){
        this.grpcProperties = grpcProperties;
        String serviceDefinPath = grpcProperties.getReferenceDefinition();
        if (StringUtils.isNoneBlank(serviceDefinPath)) {
            InputStream in = SalukiReferenceRunner.class.getClassLoader().getResourceAsStream(serviceDefinPath);
            servcieReferenceDefintions = new Gson().fromJson(new InputStreamReader(in),
                                                             new TypeToken<List<Map<String, String>>>() {
                                                             }.getType());
        } else {
            String referenceJson = System.getProperty("user.home") + "/saluki/salukireference.json";
            try {
                InputStream in = new FileInputStream(new File(referenceJson));
                servcieReferenceDefintions = new Gson().fromJson(new InputStreamReader(in),
                                                                 new TypeToken<List<Map<String, String>>>() {
                                                                 }.getType());
            } catch (FileNotFoundException e) {
                logger.warn("There is no file in the path:" + referenceJson);
                servcieReferenceDefintions = Lists.newArrayList();
            }
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
                    Object value = findServiceInSpringContainer(field.getType());
                    // 如果在spring 容器没有找到该服务，则使用远程服务
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

    private Object findServiceInSpringContainer(Class<?> referenceClass) {
        try {
            Object obj = applicationContext.getBean(referenceClass);
            return obj;
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    private Pair<String, String> findGroupAndVersion(String serviceName) {
        for (Map<String, String> referenceDefintion : servcieReferenceDefintions) {
            String servcieDefineName = referenceDefintion.get("service");
            if (servcieDefineName.equals(serviceName)) {
                String group = referenceDefintion.get("group");
                String version = referenceDefintion.get("version");
                return new ImmutablePair<String, String>(group, version);
            }
        }
        return null;
    }

    private Object refer(SalukiReference reference, Class<?> referenceClass) {
        ReferenceConfig referenceConfig = new ReferenceConfig();
        String interfaceName = reference.service();
        Pair<String, String> groupVersion = findGroupAndVersion(interfaceName);
        if (StringUtils.isNoneBlank(reference.group())) {
            referenceConfig.setGroup(reference.group());
        } else {
            Preconditions.checkNotNull(groupVersion, "Group can not be null");
            String group = groupVersion.getLeft();
            referenceConfig.setGroup(group);
        }
        if (StringUtils.isNoneBlank(reference.version())) {
            referenceConfig.setVersion(reference.version());
        } else {
            Preconditions.checkNotNull(groupVersion, "version can not be null");
            String group = groupVersion.getRight();
            referenceConfig.setGroup(group);
        }
        if (reference.retries() > 1
            && (reference.hastrategyMethod() == null || reference.hastrategyMethod().length == 0)) {
            logger.warn("Have set retries,but not have set method,will set all method to retry");
            referenceConfig.setMethodNames(new HashSet<String>(Arrays.asList(reference.hastrategyMethod())));
            referenceConfig.setReties(reference.retries());
        }
        Preconditions.checkNotNull(interfaceName, "interfaceName can not be null", interfaceName);
        referenceConfig.setInterfaceName(interfaceName);
        referenceConfig.setRegistryName("consul");
        String registryAddress = grpcProperties.getConsulIp();
        Preconditions.checkNotNull(registryAddress, "RegistryAddress can not be null", registryAddress);
        referenceConfig.setRegistryAddress(registryAddress);
        int registryPort = grpcProperties.getConsulPort();
        Preconditions.checkState(registryPort != 0, "RegistryPort can not be null", registryPort);
        referenceConfig.setRegistryPort(registryPort);
        referenceConfig.setInjvm(reference.localProcess());
        referenceConfig.setAsync(reference.callType() == 1 ? true : false);
        referenceConfig.setRequestTimeout(reference.requestTime());
        referenceConfig.setMonitorinterval(grpcProperties.getMonitorInterval());
        if (AbstractStub.class.isAssignableFrom(referenceClass)) {
            referenceConfig.setGrpcStub(true);
            referenceConfig.setInterfaceClass(referenceClass);
        } else {
            if (GenericService.class.isAssignableFrom(referenceClass)) {
                referenceConfig.setGeneric(true);
            } else {
                referenceConfig.setGeneric(false);
            }
        }
        Object value = referenceConfig.get();
        return value;
    }

}
