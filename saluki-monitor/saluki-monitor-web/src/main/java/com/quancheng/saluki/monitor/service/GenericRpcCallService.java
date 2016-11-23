package com.quancheng.saluki.monitor.service;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.quancheng.saluki.monitor.jaket.Jaket;
import com.quancheng.saluki.monitor.jaket.model.ServiceDefinition;
import com.quancheng.saluki.monitor.utils.MonitorClassLoader;

@Service
public class GenericRpcCallService {

    private static final Logger log = LoggerFactory.getLogger(GenericRpcCallService.class);

    private MonitorClassLoader  classLoader;

    @PostConstruct
    public void init() {
        classLoader = new MonitorClassLoader();
    }

    public String getService(String serviceName) {
        String path = System.getProperty("user.home") + "/saluki";
        try {
            classLoader.addClassPath();
            Class<?> clazz = classLoader.loadClass(serviceName);
            return Jaket.schema(clazz);
        } catch (ClassNotFoundException | IOException e) {
            log.error("not find service in the jar of" + path + ",please check serviceName");
        }
        return null;
    }

}
