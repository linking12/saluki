package com.quancheng.saluki.core.grpc.server;

import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.BindableService;

public class ProtocolExporterFactory {

    private static class ProtocolExporterFactoryHolder {

        private static final ProtocolExporterFactory INSTANCE = new ProtocolExporterFactory();
    }

    private ProtocolExporterFactory(){
    }

    public static final ProtocolExporterFactory getInstance() {
        return ProtocolExporterFactoryHolder.INSTANCE;
    }

    public ProtocolExporter getProtocolExporter(SalukiURL providerUrl, Object protocolImpl) {
        ProtocolExporter protocolExporter;
        if (protocolImpl instanceof BindableService) {
            protocolExporter = new StubProtocolExporter(protocolImpl.getClass(), protocolImpl);
        } else {
            Class<?> protocol;
            try {
                protocol = ReflectUtil.name2class(providerUrl.getServiceInterface());
                if (!protocol.isAssignableFrom(protocolImpl.getClass())) {
                    throw new IllegalStateException("protocolClass " + providerUrl.getServiceInterface()
                                                    + " is not implemented by protocolImpl which is of class "
                                                    + protocolImpl.getClass());
                }
            } catch (ClassNotFoundException e) {
                protocol = protocolImpl.getClass();
            }
            protocolExporter = new DefaultProtocolExporter(protocol, protocolImpl);
        }
        return protocolExporter;
    }
}
