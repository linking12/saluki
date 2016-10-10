package org.spring.boot.starter.saluki.runner;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.boot.starter.saluki.GRpcReference;
import org.spring.boot.starter.saluki.autoconfigure.GRpcProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import com.google.common.base.Preconditions;
import com.quancheng.saluki.core.config.ReferenceConfig;

public class GRpcReferenceRunner extends InstantiationAwareBeanPostProcessorAdapter {

    private static final Logger  logger = LoggerFactory.getLogger(GRpcReferenceRunner.class);

    private final GRpcProperties grpcProperties;

    public GRpcReferenceRunner(GRpcProperties grpcProperties){
        this.grpcProperties = grpcProperties;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> searchType = bean.getClass();
        while (!Object.class.equals(searchType) && searchType != null) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                GRpcReference reference = field.getAnnotation(GRpcReference.class);
                if (reference != null) {
                    Object value = refer(reference, field.getType());
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

    private Object refer(GRpcReference reference, Class<?> referenceClass) {
        ReferenceConfig referenceConfig = new ReferenceConfig();
        if (StringUtils.isNoneBlank(reference.group())) {
            referenceConfig.setGroup(reference.group());
        } else {
            String group = grpcProperties.getReferenceGroup();
            Preconditions.checkState(StringUtils.isBlank(group), "Group can not be null", group);
            referenceConfig.setGroup(group);
        }
        if (StringUtils.isNoneBlank(reference.version())) {
            referenceConfig.setVersion(reference.version());
        } else {
            String version = grpcProperties.getReferenceVersion();
            Preconditions.checkState(StringUtils.isBlank(version), "Version can not be null", version);
            referenceConfig.setVersion(version);
        }
        String interfaceName = reference.interfaceName();
        Preconditions.checkState(StringUtils.isBlank(interfaceName), "Version can not be null", interfaceName);
        referenceConfig.setInterfaceName(interfaceName);
        referenceConfig.setRegistryName("consul");
        String registryAddress = grpcProperties.getConsulIp();
        Preconditions.checkState(StringUtils.isBlank(registryAddress), "RegistryAddress can not be null",
                                 registryAddress);
        referenceConfig.setRegistryAddress(registryAddress);
        int port = grpcProperties.getConsulPort();
        Preconditions.checkState(port != 0, "RegistryAddress can not be zero", port);
        referenceConfig.setRegistryPort(grpcProperties.getConsulPort());
        referenceConfig.setInjvm(reference.localProcess());
        referenceConfig.setAsync(reference.callType() == 1 ? true : false);
        referenceConfig.setRequestTimeout(reference.requestTime());
        Object value = referenceConfig.get();
        return value;
    }

}
