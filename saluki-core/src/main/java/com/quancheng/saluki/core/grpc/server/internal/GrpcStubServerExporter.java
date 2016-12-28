/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.server.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.core.grpc.server.GrpcProtocolExporter;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

/**
 * @author shimingliu 2016年12月14日 下午10:18:38
 * @version GrpcStubServerExporter.java, v 0.0.1 2016年12月14日 下午10:18:38 shimingliu
 */
public class GrpcStubServerExporter implements GrpcProtocolExporter {

    private static final Logger log = LoggerFactory.getLogger(GrpcStubServerExporter.class);

    @Override
    public ServerServiceDefinition export(Class<?> protocol, Object protocolImpl) {
        Object obj = protocolImpl;
        if (!(obj instanceof BindableService)) {
            throw new IllegalStateException(" Object is not io.grpc.BindableService,can not export " + obj);
        } else {
            BindableService bindableService = (BindableService) obj;
            log.info("'{}' service has been registered.", bindableService.getClass().getName());
            return bindableService.bindService();
        }
    }
}
