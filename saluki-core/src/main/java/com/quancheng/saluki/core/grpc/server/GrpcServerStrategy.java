/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.server;

import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.server.internal.DefaultProxyExporter;
import com.quancheng.saluki.core.grpc.server.internal.GrpcStubServerExporter;
import com.quancheng.saluki.core.utils.ReflectUtils;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

/**
 * @author shimingliu 2016年12月14日 下午10:09:03
 * @version GrpcServerStrategy.java, v 0.0.1 2016年12月14日 下午10:09:03 shimingliu
 */
public class GrpcServerStrategy {

    private final GrpcProtocolExporter exporter;

    private final Class<?>             protocolClass;

    private final Object               protocolImpl;

    public GrpcServerStrategy(GrpcURL providerUrl, Object protocolImpl){
        if (protocolImpl instanceof BindableService) {
            this.exporter = new GrpcStubServerExporter();
            this.protocolClass = protocolImpl.getClass();
        } else {
            Class<?> protocol;
            try {
                protocol = ReflectUtils.name2class(providerUrl.getServiceInterface());
                if (!protocol.isAssignableFrom(protocolImpl.getClass())) {
                    throw new IllegalStateException("protocolClass " + providerUrl.getServiceInterface()
                                                    + " is not implemented by protocolImpl which is of class "
                                                    + protocolImpl.getClass());
                }
            } catch (ClassNotFoundException e) {
                protocol = protocolImpl.getClass();
            }
            this.protocolClass = protocol;
            this.exporter = new DefaultProxyExporter(providerUrl);
        }
        this.protocolImpl = protocolImpl;
    }

    public ServerServiceDefinition getServerDefintion() {
        return exporter.export(protocolClass, protocolImpl);
    }
}
