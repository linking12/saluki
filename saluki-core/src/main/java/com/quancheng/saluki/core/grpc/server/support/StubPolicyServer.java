package com.quancheng.saluki.core.grpc.server.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.core.grpc.server.GrpcProtocolExporter;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

public class StubPolicyServer implements GrpcProtocolExporter {

    private static final Logger log = LoggerFactory.getLogger(GrpcProtocolExporter.class);

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
