/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.grpc;

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
public class GrpcRemoteComponent {

    private static final Logger                logger             = LoggerFactory.getLogger(GrpcRemoteComponent.class);

    private static final Map<String, Class<?>> remoteServiceCache = Maps.newConcurrentMap();

    private static final Gson                  GSON               = new Gson();

    @SalukiReference(group = "default", version = "1.0.0")
    private GenericService                     genricService;

    public Object callRemoteService(String serviceName, String group, String version, String methodName,
                                    String requestParam) throws Throwable {
        try {
            Class<?> serviceClass = remoteServiceCache.get(serviceName);
            if (serviceClass == null) {
                GrpcClassLoader classLoader = new GrpcClassLoader();
                classLoader.setSystemClassLoader(Thread.currentThread().getContextClassLoader());
                serviceClass = ReflectUtils.name2class(classLoader, serviceName);
                remoteServiceCache.put(serviceName, serviceClass);
            }
            Method method = ReflectUtils.findMethodByMethodName(serviceClass, methodName);
            Class<?> requestType = method.getParameterTypes()[0];
            Class<?> returnType = method.getReturnType();
            Object request = GSON.fromJson(requestParam, requestType);
            String[] paramTypes = new String[] { requestType.getName(), returnType.getName() };
            Object[] args = new Object[] { request };
            Object reply = genricService.$invoke(serviceName, group, version, methodName, paramTypes, args);
            return reply;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

}
