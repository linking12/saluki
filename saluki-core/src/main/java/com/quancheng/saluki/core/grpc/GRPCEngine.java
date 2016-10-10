package com.quancheng.saluki.core.grpc;

import java.util.Map;

import com.quancheng.saluki.core.common.SalukiURL;

public interface GRPCEngine {

    Object getProxy(SalukiURL refUrl) throws Exception;

    SalukiServer getServer(Map<SalukiURL, Object> providerUrls, int port) throws Exception;

}
