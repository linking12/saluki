package com.quancheng.boot.saluki.starter.runner;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.quancheng.boot.saluki.starter.SalukiService;
import com.quancheng.boot.saluki.starter.autoconfigure.SalukiProperties;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.config.ServiceConfig;

@Order(value = 0)
public class SalukiServerRunner implements CommandLineRunner, DisposableBean {

    private static final Logger        log = LoggerFactory.getLogger(SalukiServerRunner.class);

    @Autowired
    private SalukiProperties           grpcProperties;

    @Autowired
    private AbstractApplicationContext applicationContext;

    private ServiceConfig              serviceConfig;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting gRPC Server ...");
        resetRegistryPort();
        ServiceConfig serviceConfig = newServiceConfig();
        for (Object obj : getTypedBeansWithAnnotation(SalukiService.class)) {
            SalukiService gRpcServiceAnn = obj.getClass().getAnnotation(SalukiService.class);
            String interfaceName = gRpcServiceAnn.service();
            if (StringUtils.isBlank(interfaceName)) {
                interfaceName = obj.getClass().getName();
            }
            serviceConfig.addServiceConfig(interfaceName, this.getGroup(gRpcServiceAnn),
                                           this.getVersion(gRpcServiceAnn), obj);
        }
        serviceConfig.export();
    }

    @Override
    public void destroy() throws Exception {
        serviceConfig.destroy();
        applicationContext.destroy();
    }

    private String getGroup(SalukiService service) {
        if (StringUtils.isNoneBlank(service.group())) {
            return service.group();
        } else {
            String group = grpcProperties.getServiceGroup();
            Preconditions.checkNotNull(group, "Group can not be null", group);
            return group;
        }
    }

    private String getVersion(SalukiService service) {
        if (StringUtils.isNoneBlank(service.version())) {
            return service.version();
        } else {
            String version = grpcProperties.getServcieVersion();
            Preconditions.checkNotNull(version, "Version can not be null", version);
            return version;
        }
    }

    private void resetRegistryPort() {
        int serverRegistryPort = grpcProperties.getServerRegistryPort();
        if (serverRegistryPort != 0) {
            System.setProperty(SalukiConstants.REGISTRY_SERVER_PORT, Integer.valueOf(serverRegistryPort).toString());
        }
    }

    private ServiceConfig newServiceConfig() {
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setRegistryName("consul");
        String registryAddress = grpcProperties.getConsulIp();
        Preconditions.checkNotNull(registryAddress, "RegistryAddress can not be null", registryAddress);
        serviceConfig.setRegistryAddress(registryAddress);
        int port = grpcProperties.getConsulPort();
        Preconditions.checkState(port != 0, "RegistryPort can not be zero", port);
        serviceConfig.setRegistryPort(grpcProperties.getConsulPort());
        int serverPort = grpcProperties.getServerStartPort();
        Preconditions.checkState(serverPort != 0, "ServerPort can not be null", serverPort);
        serviceConfig.setPort(serverPort);
        serviceConfig.setHost(grpcProperties.getServerHost());
        serviceConfig.setMonitorinterval(grpcProperties.getMonitorInterval());
        return serviceConfig;
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
