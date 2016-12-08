package com.quancheng.saluki.monitor.web;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class MonitorWebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/serviceMeasure/dist/**")//
                .addResourceLocations("classpath:/META-INF/resources/static/dist/");
        registry.addResourceHandler("/serviceMeasure.html").//
                addResourceLocations("classpath:/META-INF/resources/static/");
        registry.addResourceHandler("/webjars/**")//
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
