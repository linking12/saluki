package com.quancheng.saluki.core.grpc;

import java.util.Map;

import com.quancheng.saluki.core.common.SalukiURL;

import io.grpc.Server;

public interface GRPCEngine {

    Object getProxy(SalukiURL refUrl) throws Exception;

    Server getServer(Map<SalukiURL, Object> providerUrls, int port) throws Exception;

}
