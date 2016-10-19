package com.quancheng.saluki.core.grpc.server;

import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.server.support.DefaultPolicyExporter;
import com.quancheng.saluki.core.grpc.server.support.StubPolicyServer;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

public class GrpcServerContext {

    private final GrpcProtocolExporter exporter;

    private final Class<?>             protocolClass;

    private final Object               protocolImpl;

    public GrpcServerContext(SalukiURL providerUrl, Object protocolImpl){
        if (protocolImpl instanceof BindableService) {
            this.exporter = new StubPolicyServer();
            this.protocolClass = protocolImpl.getClass();
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
            this.protocolClass = protocol;
            this.exporter = new DefaultPolicyExporter();
        }
        this.protocolImpl = protocolImpl;
    }

    public ServerServiceDefinition getServerDefintion() {
        return exporter.export(protocolClass, protocolImpl);
    }
}
