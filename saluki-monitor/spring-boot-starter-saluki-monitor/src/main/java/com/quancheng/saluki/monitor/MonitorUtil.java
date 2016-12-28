/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.monitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author shimingliu 2016年12月20日 下午3:01:31
 * @version MonitorUtil.java, v 0.0.1 2016年12月20日 下午3:01:31 shimingliu
 */
public final class MonitorUtil {

    private static final long                               SECOND = 1000;

    private static final long                               MINUTE = 60 * SECOND;

    private static final long                               HOUR   = 60 * MINUTE;

    private static final long                               DAY    = 24 * HOUR;

    private static final AnnotationConfigApplicationContext ctx;

    private MonitorUtil(){

    }

    static {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(MybatisConfiguration.class);
        ctx.register(MybatisMapperScannerConfig.class);
        ctx.refresh();

    }

    public static <T> T getBean(Class<T> requiredType) {
        return ctx.getBean(requiredType);
    }

    public static Date parse(String string) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(string);
        } catch (ParseException e) {
            throw new java.lang.IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static String formatUptime(long uptime) {
        StringBuilder buf = new StringBuilder();
        if (uptime > DAY) {
            long days = (uptime - uptime % DAY) / DAY;
            buf.append(days);
            buf.append(" Days");
            uptime = uptime % DAY;
        }
        if (uptime > HOUR) {
            long hours = (uptime - uptime % HOUR) / HOUR;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(hours);
            buf.append(" Hours");
            uptime = uptime % HOUR;
        }
        if (uptime > MINUTE) {
            long minutes = (uptime - uptime % MINUTE) / MINUTE;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(minutes);
            buf.append(" Minutes");
            uptime = uptime % MINUTE;
        }
        if (uptime > SECOND) {
            long seconds = (uptime - uptime % SECOND) / SECOND;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(seconds);
            buf.append(" Seconds");
            uptime = uptime % SECOND;
        }
        if (uptime > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(uptime);
            buf.append(" Milliseconds");
        }
        return buf.toString();
    }

    public static String createUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
