/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.grpc.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.core.grpc.client.GrpcClassLoader;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.core.utils.ReflectUtils;

@Component
@SuppressWarnings("resource")
public class GrpcRemoteComponent {

    private static final Logger                logger             = LoggerFactory.getLogger(GrpcRemoteComponent.class);

    private static final Map<String, Class<?>> remoteServiceCache = Maps.newConcurrentMap();

    private static final Gson                  GSON               = new Gson();

    @SalukiReference(group = "default", version = "1.0.0")
    private GenericService                     genricService;

    public String callRemoteService(String serviceName, String group, String version, String methodName,
                                    String requestParam) throws Throwable {
        try {
            assert (serviceName != null);
            assert (group != null);
            assert (version != null);
            assert (methodName != null);
            assert (requestParam != null && !requestParam.isEmpty());
            Class<?> serviceClass = remoteServiceCache.get(serviceName);
            if (serviceClass == null) {
                GrpcClassLoader classLoader = new GrpcClassLoader();
                classLoader.setSystemClassLoader(Thread.currentThread().getContextClassLoader());
                serviceClass = classLoader.loadClass(serviceName);
                remoteServiceCache.put(serviceName, serviceClass);
            }
            Method method = ReflectUtils.findMethodByMethodName(serviceClass, methodName);
            Class<?> requestType = method.getParameterTypes()[0];
            Class<?> returnType = method.getReturnType();
            Object request = GSON.fromJson(requestParam, requestType);
            String[] paramTypes = new String[] { requestType.getName(), returnType.getName() };
            Object[] args = new Object[] { request };
            Object reply = genricService.$invoke(serviceName, group, version, methodName, paramTypes, args);
            return GSON.toJson(reply);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    public String callRemoteService(String serviceName, String group, String version, String methodName,
                                    Map<String, String> requestParam) throws Throwable {
        try {
            assert (serviceName != null);
            assert (group != null);
            assert (version != null);
            assert (methodName != null);
            assert (requestParam != null && !requestParam.isEmpty());
            Class<?> serviceClass = remoteServiceCache.get(serviceName);
            if (serviceClass == null) {
                GrpcClassLoader classLoader = new GrpcClassLoader();
                classLoader.setSystemClassLoader(Thread.currentThread().getContextClassLoader());
                serviceClass = classLoader.loadClass(serviceName);
                remoteServiceCache.put(serviceName, serviceClass);
            }
            Method method = ReflectUtils.findMethodByMethodName(serviceClass, methodName);
            Class<?> requestType = method.getParameterTypes()[0];
            Class<?> returnType = method.getReturnType();
            Object request = ReflectUtils.getEmptyObject(requestType);
            for (Map.Entry<String, String> entry : requestParam.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                Field field = requestType.getDeclaredField(fieldName);
                if (String.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    field.set(request, fieldValue);
                } else {
                    throw new IllegalArgumentException("only support String");
                }
            }
            String[] paramTypes = new String[] { requestType.getName(), returnType.getName() };
            Object[] args = new Object[] { request };
            Object reply = genricService.$invoke(serviceName, group, version, methodName, paramTypes, args);
            return GSON.toJson(reply);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

}
