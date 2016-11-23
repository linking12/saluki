package com.quancheng.saluki.monitor.service;

import java.io.IOException;

import com.google.gson.Gson;
import com.quancheng.saluki.monitor.service.support.model.MethodDefinition;
import com.quancheng.saluki.monitor.utils.MonitorClassLoader;

public class TestMain {

    private static MonitorClassLoader classLoader;

    static {
        classLoader = new MonitorClassLoader();
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        // classLoader.addClassPath();
        // Class<?> clazz = classLoader.loadClass("com.quancheng.examples.service.HelloService");
        // ServiceDefinition sd = Jaket.build(clazz);
        // System.out.println(new Gson().toJson(sd));
        GenericRpcCallService service = new GenericRpcCallService();
        service.init();
        MethodDefinition md = service.getMethod("com.quancheng.examples.service.HelloService", "sayHello");
        System.out.println(new Gson().toJson(md));
    }

}
