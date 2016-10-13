package com.quancheng.saluki.core.grpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

public class StubProtocolExporter extends AbstractProtocolExporter {

    private static final Logger log = LoggerFactory.getLogger(ProtocolExporter.class);

    public StubProtocolExporter(Class<?> protocolClass, Object protocolImpl){
        super(protocolClass, protocolImpl);
    }

    @Override
    public ServerServiceDefinition doExport() {
        Object obj = getProtocolImpl();
        if (!(obj instanceof BindableService)) {
            throw new IllegalStateException(" Object is not io.grpc.BindableService,can not export " + obj);
        } else {
            BindableService bindableService = (BindableService) obj;
            log.info("'{}' service has been registered.", bindableService.getClass().getName());
            return bindableService.bindService();
        }
    }

}
