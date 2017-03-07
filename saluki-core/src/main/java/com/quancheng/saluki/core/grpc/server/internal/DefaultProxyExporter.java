/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.server.internal;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.server.GrpcProtocolExporter;
import com.quancheng.saluki.core.grpc.service.ClientServerMonitor;
import com.quancheng.saluki.core.grpc.service.MonitorService;
import com.quancheng.saluki.core.grpc.util.GrpcReflectUtil;
import com.quancheng.saluki.core.grpc.util.MethodDescriptorUtil;

import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;

/**
 * @author shimingliu 2016年12月14日 下午10:10:33
 * @version DefaultProxyExporter.java, v 0.0.1 2016年12月14日 下午10:10:33 shimingliu
 */
public class DefaultProxyExporter implements GrpcProtocolExporter {

    private static final Logger  log = LoggerFactory.getLogger(DefaultProxyExporter.class);

    private final GrpcURL        providerUrl;

    private final MonitorService clientServerMonitor;

    public DefaultProxyExporter(GrpcURL providerUrl){
        this.clientServerMonitor = new ClientServerMonitor(providerUrl);
        this.providerUrl = providerUrl;
    }

    @Override
    public ServerServiceDefinition export(Class<?> protocol, Object protocolImpl) {
        Class<?> serivce = protocol;
        Object serviceRef = protocolImpl;
        String serviceName = protocol.getName();
        ServerServiceDefinition.Builder serviceDefBuilder = ServerServiceDefinition.builder(serviceName);
        List<Method> methods = GrpcReflectUtil.findAllPublicMethods(serivce);
        if (methods.isEmpty()) {
            throw new IllegalStateException("protocolClass " + serviceName + " not have export method" + serivce);
        }
        final ConcurrentMap<String, AtomicInteger> concurrents = new ConcurrentHashMap<String, AtomicInteger>();
        for (Method method : methods) {
            MethodDescriptor<Message, Message> methodDescriptor = MethodDescriptorUtil.createMethodDescriptor(serivce,
                                                                                                              method);
            serviceDefBuilder.addMethod(methodDescriptor,
                                        ServerCalls.asyncUnaryCall(new ServerInvocation(serviceRef, method, providerUrl,
                                                                                        concurrents, clientServerMonitor)));
        }
        log.info("'{}' service has been registered.", serviceName);
        return serviceDefBuilder.build();
    }

}
