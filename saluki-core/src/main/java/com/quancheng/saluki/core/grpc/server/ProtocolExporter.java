package com.quancheng.saluki.core.grpc.server;

import io.grpc.ServerServiceDefinition;

public interface ProtocolExporter {

    public ServerServiceDefinition doExport();
}
