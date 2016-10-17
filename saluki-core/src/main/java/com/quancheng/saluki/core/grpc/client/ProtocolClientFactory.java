package com.quancheng.saluki.core.grpc.client;

import java.util.concurrent.Callable;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.SalukiClassLoader;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.Channel;

public class ProtocolClientFactory {

    private final SalukiClassLoader      classLoader;

    private final Cache<String, Channel> channelCache;

    private static class ProtocolProxyFactoryHolder {

        private static final ProtocolClientFactory INSTANCE = new ProtocolClientFactory();
    }

    private ProtocolClientFactory(){
        this.classLoader = new SalukiClassLoader();
        this.channelCache = CacheBuilder.newBuilder()//
                                        .maximumSize(5000L)//
                                        .softValues()//
                                        .ticker(Ticker.systemTicker())//
                                        .build();
    }

    public static final ProtocolClientFactory getInstance() {
        return ProtocolProxyFactoryHolder.INSTANCE;
    }

    public ProtocolClient<Object> getProtocolProxy(SalukiURL refUrl, Callable<Channel> channelCallable) {
        boolean isGeneric = refUrl.getParameter(SalukiConstants.GENERIC_KEY, SalukiConstants.DEFAULT_GENERIC);
        int rpcType = refUrl.getParameter(SalukiConstants.RPCTYPE_KEY, SalukiConstants.RPCTYPE_ASYNC);
        int rpcTimeOut = refUrl.getParameter(SalukiConstants.RPCTIMEOUT_KEY, SalukiConstants.DEFAULT_TIMEOUT);
        boolean stub = refUrl.getParameter(SalukiConstants.GRPC_STUB_KEY, Boolean.FALSE);
        String protocol = refUrl.getServiceInterface();
        String protocolClass = refUrl.getParameter(SalukiConstants.INTERFACECLASS_KEY, protocol);
        if (isGeneric) {
            GeneralizeProxyClient genericProxy = new GeneralizeProxyClient(channelCache, protocol, channelCallable,
                                                                           rpcTimeOut, rpcType);
            genericProxy.setSalukiClassLoader(classLoader);
            return genericProxy;
        } else {
            try {
                Class<?> protocolClazz = ReflectUtil.name2class(protocolClass);
                if (stub) {
                    GrpcStubClient<Object> stubClient = new GrpcStubClient<Object>(channelCache, protocol,
                                                                                   channelCallable, rpcTimeOut,
                                                                                   rpcType);
                    stubClient.setProtocolClass(protocolClazz);
                    return stubClient;
                } else {
                    JavaProxyClient<Object> javaProxyClient = new JavaProxyClient<Object>(channelCache, protocol,
                                                                                          channelCallable, rpcTimeOut,
                                                                                          rpcType);
                    javaProxyClient.setProtocolClass(protocolClazz);
                    return javaProxyClient;
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
    }
}
