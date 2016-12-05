package com.quancheng.boot.saluki.starter.autoconfigure;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.google.gson.Gson;
import com.quancheng.boot.saluki.starter.SalukiService;
import com.quancheng.boot.saluki.starter.runner.SalukiReferenceRunner;
import com.quancheng.boot.saluki.starter.runner.SalukiServerRunner;
import com.quancheng.saluki.core.common.SalukiConstants;

@Configuration
@ConditionalOnProperty(prefix = "grpc", name = "consulIp")
@EnableConfigurationProperties(SalukiProperties.class)
public class SalukiAutoConfiguration {

    private final SalukiProperties grpcProperty;

    public SalukiAutoConfiguration(SalukiProperties grpcProperty, Environment env){
        this.grpcProperty = grpcProperty;
        if (StringUtils.isBlank(grpcProperty.getApplication())) {
            String appName = env.getProperty("spring.application.name");
            grpcProperty.setApplication(appName);
        }
    }

    @Bean
    @ConditionalOnBean(value = SalukiProperties.class, annotation = SalukiService.class)
    public SalukiServerRunner grpcServerRunner() {
        return new SalukiServerRunner();
    }

    @Bean
    public BeanPostProcessor grpcReferenceRunner() {
        return new SalukiReferenceRunner(grpcProperty);
    }

    @Configuration
    public class PortListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

        @Override
        public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
            Properties serverInfo = new Properties();
            int httpPort = event.getEmbeddedServletContainer().getPort();
            String realHttpPort = System.getProperty("HTTP_PORT", String.valueOf(httpPort));
            serverInfo.setProperty("serverHttpPort", realHttpPort);
            if (StringUtils.isNotBlank(grpcProperty.getClientHost())) {
                serverInfo.setProperty("serverHost", String.valueOf(grpcProperty.getClientHost()));
            }
            if (StringUtils.isNoneBlank(grpcProperty.getServerHost())) {
                serverInfo.setProperty("serverHost", String.valueOf(grpcProperty.getServerHost()));
            }
            System.setProperty(SalukiConstants.REGISTRY_SERVER_PARAM, new Gson().toJson(serverInfo));
        }
    }

}
