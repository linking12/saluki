package com.quancheng.saluki.core.grpc;

import java.util.Map;

import com.quancheng.saluki.core.common.SalukiURL;

public interface GRPCEngine {

    <T> ProtocolProxy<T> getProxy(SalukiURL refUrl) throws Exception;

    io.grpc.Server getServer(Map<SalukiURL, Object> providerUrls, int port) throws Exception;

}
