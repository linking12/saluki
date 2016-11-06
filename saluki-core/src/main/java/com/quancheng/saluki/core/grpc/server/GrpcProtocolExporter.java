package com.quancheng.saluki.core.grpc.server;

import io.grpc.ServerServiceDefinition;

public interface GrpcProtocolExporter {

    public ServerServiceDefinition export(Class<?> protocol, Object protocolImpl);
}
