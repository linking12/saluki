package com.quancheng.boot.saluki.starter.autoconfigure;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.quancheng.boot.saluki.starter.SalukiService;
import com.quancheng.boot.saluki.starter.runner.SalukiReferenceRunner;
import com.quancheng.boot.saluki.starter.runner.SalukiServerRunner;
import com.quancheng.saluki.core.common.SalukiConstants;

@Configuration
@ConditionalOnProperty(prefix = "grpc", name = "consulIp")
@EnableConfigurationProperties(SalukiProperties.class)
public class SalukiAutoConfiguration {

    private final SalukiProperties grpcProperty;

    public SalukiAutoConfiguration(SalukiProperties grpcProperty){
        this.grpcProperty = grpcProperty;
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
    public static class PortListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

        @Override
        public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
            int serverPort = event.getEmbeddedServletContainer().getPort();
            if (serverPort != 0) {
                System.setProperty(SalukiConstants.REGISTRY_CLIENT_PORT, String.valueOf(serverPort));
            }
        }
    }

}
