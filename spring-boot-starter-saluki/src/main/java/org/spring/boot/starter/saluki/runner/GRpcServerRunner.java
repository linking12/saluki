package org.spring.boot.starter.saluki.runner;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.boot.starter.saluki.GRpcService;
import org.spring.boot.starter.saluki.autoconfigure.GRpcProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.quancheng.saluki.core.config.ReferenceConfig;
import com.quancheng.saluki.core.config.ServiceConfig;

@Order(value = 0)
public class GRpcServerRunner implements CommandLineRunner, DisposableBean {

    private static final Logger        log = LoggerFactory.getLogger(GRpcServerRunner.class);

    @Autowired
    private GRpcProperties             grpcProperties;

    @Autowired
    private AbstractApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting gRPC Server ...");

    }

    @Override
    public void destroy() throws Exception {
        ServiceConfig serviceConfig = new ServiceConfig();
        for (Object obj : getTypedBeansWithAnnotation(GRpcService.class)) {
            GRpcService gRpcServiceAnn = obj.getClass().getAnnotation(GRpcService.class);
            String interfaceName = gRpcServiceAnn.interfaceName();
            if (StringUtils.isBlank(interfaceName)) {
                interfaceName = obj.getClass().getName();
            }
            serviceConfig.addRef(interfaceName, obj);
        }
        serviceConfig.export();
    }

    private void export(GRpcService service) {
        if (StringUtils.isNoneBlank(service.group())) {
            referenceConfig.setGroup(service.group());
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
