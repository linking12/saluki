/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;

/**
 * @author shimingliu 2016年12月20日 下午3:44:35
 * @version MonitorAutoconfiguration.java, v 0.0.1 2016年12月20日 下午3:44:35 shimingliu
 */
@Configuration
@ConditionalOnExpression("${saluki.monitor.enabled:true}")
public class MonitorAutoconfiguration {

    private static final Logger log = LoggerFactory.getLogger(MonitorAutoconfiguration.class);

    @Bean
    public BeanFactoryPostProcessor beanFactoryPostProcessor(ApplicationContext applicationContext) {
        return new BeanFactoryPostProcessor() {

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                if (beanFactory instanceof BeanDefinitionRegistry) {
                    try {
                        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
                        scanner.setResourceLoader(applicationContext);
                        scanner.scan("com.quancheng.saluki.boot.web");
                    } catch (Throwable e) {
                        log.error(e.getMessage(), e);
                    }
                }

            }

        };
    }

    @Configuration
    @AutoConfigureAfter(WebMvcAutoConfiguration.class)
    public static class WebMvcAutoconfig extends WebMvcConfigurerAdapter {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/assets/**")//
                    .addResourceLocations("classpath:/META-INF/static/assets/");
            registry.addResourceHandler("doc.html").//
                    addResourceLocations("classpath:/META-INF/static/");
        }

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/doc").setViewName("/doc.html");
        }

    };

    @Configuration
    @AutoConfigureAfter(WebMvcAutoConfiguration.class)
    public static class HystrixEventStreamConfig {

        @Bean
        public HystrixMetricsStreamServlet hystrixMetricsStreamServlet() {
            return new HystrixMetricsStreamServlet();
        }

        @Bean
        public ServletRegistrationBean registration(HystrixMetricsStreamServlet servlet) {
            ServletRegistrationBean registrationBean = new ServletRegistrationBean();
            registrationBean.setServlet(servlet);
            registrationBean.setEnabled(true);
            registrationBean.addUrlMappings("/hystrix.stream");
            return registrationBean;
        }
    }

}
