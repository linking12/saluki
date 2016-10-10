package com.quancheng.saluki.core.grpc.server;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

public class StubProtocolExporter extends AbstractProtocolExporter {

    public StubProtocolExporter(Class<?> protocolClass, Object protocolImpl){
        super(protocolClass, protocolImpl);
    }

    @Override
    public ServerServiceDefinition doExport() {
        Object obj = this.getProtocolImpl();
        if (!(obj instanceof BindableService)) {
            throw new IllegalStateException(" Object is not io.grpc.BindableService,can not export " + obj);
        } else {
            BindableService bindableService = (BindableService) obj;
            return bindableService.bindService();
        }
    }

}
