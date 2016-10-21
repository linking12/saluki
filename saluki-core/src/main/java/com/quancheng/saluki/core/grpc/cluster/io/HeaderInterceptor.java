package com.quancheng.saluki.core.grpc.cluster.io;

import io.grpc.Metadata;

public interface HeaderInterceptor {

    void updateHeaders(Metadata headers) throws Exception;
}
