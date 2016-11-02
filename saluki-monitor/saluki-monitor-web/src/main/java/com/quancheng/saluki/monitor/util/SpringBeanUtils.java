package com.quancheng.saluki.monitor.util;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.quancheng.saluki.monitor.config.MyBatisConfig;
import com.quancheng.saluki.monitor.config.MyBatisMapperScannerConfig;

public class SpringBeanUtils {

    private static final AnnotationConfigApplicationContext ctx;

    static {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(MyBatisConfig.class);
        ctx.register(MyBatisMapperScannerConfig.class);
        ctx.refresh();
    }

    public static <T> T getBean(Class<T> requiredType) {
        return ctx.getBean(requiredType);
    }
}
