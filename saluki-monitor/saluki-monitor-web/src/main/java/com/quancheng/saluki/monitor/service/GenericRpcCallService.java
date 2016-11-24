package com.quancheng.saluki.monitor.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.monitor.model.GenericModel;
import com.quancheng.saluki.monitor.service.support.Jaket;
import com.quancheng.saluki.monitor.service.support.model.MethodDefinition;
import com.quancheng.saluki.monitor.service.support.model.ServiceDefinition;
import com.quancheng.saluki.monitor.service.support.model.TypeDefinition;
import com.quancheng.saluki.monitor.utils.MonitorClassLoader;

@Service
public class GenericRpcCallService {

    private static final Logger log = LoggerFactory.getLogger(GenericRpcCallService.class);

    private MonitorClassLoader  classLoader;

    @SalukiReference(service = "com.quancheng.saluki.core.grpc.service.GenericService", group = "Generic", version = "1.0.0")
    private GenericService      genricService;

    private Gson                gson;

    @PostConstruct
    public void init() {
        classLoader = new MonitorClassLoader();
        gson = new Gson();
    }

    public Object callRemoteService(GenericModel model) throws ClassNotFoundException {
        String serviceName = model.getServiceName();
        String group = model.getGroup();
        String version = model.getVersion();
        String method = model.getMethod();
        List<String> parameterTypesList = model.getParameters();
        List<String> parameters = model.getParameters();
        String[] parameterTypesArray = (String[]) parameterTypesList.toArray(new String[parameterTypesList.size()]);
        String requestType = parameterTypesList.get(0);
        doAddJarIntoClassPath();
        Class<?> clazz = classLoader.loadClass(requestType);
        Object[] args = new Object[] { gson.fromJson(parameters.get(0), clazz) };
        return genricService.$invoke(serviceName, group, version, method, parameterTypesArray, args);
    }

    public List<MethodDefinition> getAllMethod(String serviceName) {
        try {
            doAddJarIntoClassPath();
            Class<?> clazz = classLoader.loadClass(serviceName);
            ServiceDefinition sd = Jaket.build(clazz);
            return sd.getMethods();
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public MethodDefinition getMethod(String serviceName, String methodName) {
        try {
            doAddJarIntoClassPath();
            Class<?> clazz = classLoader.loadClass(serviceName);
            ServiceDefinition serviceMeta = Jaket.build(clazz);
            List<MethodDefinition> methodMetas = serviceMeta.getMethods();
            MethodDefinition targetMethodMeta = null;
            for (MethodDefinition methodMeta : methodMetas) {
                if (methodMeta.getName().equals(methodName)) {
                    targetMethodMeta = methodMeta;
                    break;
                }
            }
            String[] requestTypes = targetMethodMeta.getParameterTypes();
            List<TypeDefinition> parameters = new ArrayList<TypeDefinition>();
            targetMethodMeta.setParameters(parameters);
            for (String requestType : requestTypes) {
                for (TypeDefinition parameterMeta : serviceMeta.getTypes()) {
                    if (parameterMeta.getType().equals(requestType)) {
                        parameters.add(parameterMeta);
                    }
                }
            }
            return targetMethodMeta;
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private void doAddJarIntoClassPath() {
        String path = System.getProperty("user.home") + "/saluki";
        try {
            classLoader.addClassPath();
        } catch (IOException e) {
            log.error("not find service in the jar of" + path + ",please check serviceName");
        }
    }
}
