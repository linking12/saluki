package com.quancheng.saluki.core.grpc;

import io.grpc.ServerServiceDefinition;

public interface ProtocolExporter {

    public ServerServiceDefinition doExport();
}
