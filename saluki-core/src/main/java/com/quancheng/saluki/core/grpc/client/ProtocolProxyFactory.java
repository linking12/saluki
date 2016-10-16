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

public class ProtocolProxyFactory {

    private final SalukiClassLoader      classLoader;

    private final Cache<String, Channel> channelCache;

    private static class ProtocolProxyFactoryHolder {

        private static final ProtocolProxyFactory INSTANCE = new ProtocolProxyFactory();
    }

    private ProtocolProxyFactory(){
        this.classLoader = new SalukiClassLoader();
        this.channelCache = CacheBuilder.newBuilder()//
                                        .maximumSize(5000L)//
                                        .softValues()//
                                        .ticker(Ticker.systemTicker())//
                                        .build();
    }

    public static final ProtocolProxyFactory getInstance() {
        return ProtocolProxyFactoryHolder.INSTANCE;
    }

    public ProtocolClient<Object> getProtocolProxy(SalukiURL refUrl, Callable<Channel> channelCallable) {
        boolean isGeneric = refUrl.getParameter(SalukiConstants.GENERIC_KEY, SalukiConstants.DEFAULT_GENERIC);
        int rpcType = refUrl.getParameter(SalukiConstants.RPCTYPE_KEY, SalukiConstants.RPCTYPE_ASYNC);
        int rpcTimeOut = refUrl.getParameter(SalukiConstants.RPCTIMEOUT_KEY, SalukiConstants.DEFAULT_TIMEOUT);
        boolean stub = refUrl.getParameter(SalukiConstants.GRPC_STUB_KEY, Boolean.FALSE);
        String protocol = refUrl.getServiceInterface();
        String protocolClass = refUrl.getParameter(SalukiConstants.INTERFACECLASS_KEY, protocol);
        try {
            Class<?> protocolClazz = ReflectUtil.name2class(protocolClass);
            if (isGeneric) {
                GeneralizeProxyClient genericProxy = new GeneralizeProxyClient(channelCache, protocol, protocolClazz,
                                                                               channelCallable, rpcTimeOut, rpcType);
                genericProxy.setSalukiClassLoader(classLoader);
                return genericProxy;
            } else {
                if (stub) {
                    return new GrpcStubClient<Object>(channelCache, protocol, protocolClazz, channelCallable,
                                                      rpcTimeOut, rpcType);
                } else {
                    return new JavaProxyClient<Object>(channelCache, protocol, protocolClazz, channelCallable,
                                                       rpcTimeOut, rpcType);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

    }

}
