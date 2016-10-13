package com.quancheng.saluki.core.grpc.proxy;

import java.util.concurrent.Callable;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.SalukiClassLoader;

import io.grpc.Channel;

public class ProtocolProxyFactory {

    private final SalukiClassLoader classLoader;

    private static class ProtocolProxyFactoryHolder {

        private static final ProtocolProxyFactory INSTANCE = new ProtocolProxyFactory();
    }

    private ProtocolProxyFactory(){
        classLoader = new SalukiClassLoader();
    }

    public static final ProtocolProxyFactory getInstance() {
        return ProtocolProxyFactoryHolder.INSTANCE;
    }

    public ProtocolProxy<Object> getProtocolProxy(SalukiURL refUrl, Callable<Channel> channelCallable) {
        boolean isGeneric = refUrl.getParameter(SalukiConstants.GENERIC_KEY, SalukiConstants.DEFAULT_GENERIC);
        int rpcType = refUrl.getParameter(SalukiConstants.RPCTYPE_KEY, SalukiConstants.RPCTYPE_ASYNC);
        int rpcTimeOut = refUrl.getParameter(SalukiConstants.RPCTIMEOUT_KEY, SalukiConstants.DEFAULT_TIMEOUT);
        boolean stub = refUrl.getParameter(SalukiConstants.GRPC_STUB_KEY, Boolean.FALSE);
        String protocol = refUrl.getServiceInterface();
        if (isGeneric) {
            GenericProxy genericProxy = new GenericProxy(protocol, channelCallable, rpcTimeOut, rpcType, isGeneric);
            genericProxy.setSalukiClassLoader(classLoader);
            return genericProxy;
        } else {
            if (stub) {
                return new StubObject<Object>(protocol, channelCallable, rpcTimeOut, rpcType);
            } else {
                return new NormalProxy<Object>(protocol, channelCallable, rpcTimeOut, rpcType);
            }
        }
    }
}
