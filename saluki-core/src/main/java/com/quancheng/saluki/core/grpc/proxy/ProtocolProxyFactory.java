package com.quancheng.saluki.core.grpc.proxy;

import java.lang.reflect.Modifier;
import java.util.concurrent.Callable;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.Channel;

public class ProtocolProxyFactory {

    private static class ProtocolProxyFactoryHolder {

        private static final ProtocolProxyFactory INSTANCE = new ProtocolProxyFactory();
    }

    private ProtocolProxyFactory(){
    }

    public static final ProtocolProxyFactory getInstance() {
        return ProtocolProxyFactoryHolder.INSTANCE;
    }

    public ProtocolProxy<Object> getProtocolProxy(SalukiURL refUrl, Callable<Channel> channelCallable) {
        boolean isGeneric = refUrl.getParameter(SalukiConstants.GENERIC_KEY, SalukiConstants.DEFAULT_GENERIC);
        int rpcType = refUrl.getParameter(SalukiConstants.RPCTYPE_KEY, SalukiConstants.RPCTYPE_ASYNC);
        int rpcTimeOut = refUrl.getParameter(SalukiConstants.RPCTIMEOUT_KEY, SalukiConstants.DEFAULT_TIMEOUT);
        String protocol = refUrl.getServiceInterface();
        Boolean isInterface = false;
        try {
            Class<?> protocolClzz = ReflectUtil.name2class(protocol);
            isInterface = Modifier.isInterface(protocolClzz.getModifiers());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("no class find in classpath", e);
        }
        if (isGeneric) {
            return new GenericProxy(protocol, channelCallable, rpcTimeOut, rpcType, isGeneric);
        }
        if (isInterface) {
            return new NormalProxy<Object>(protocol, channelCallable, rpcTimeOut, rpcType, isGeneric);
        } else {
            return new StubObject<Object>(protocol, channelCallable, rpcTimeOut, rpcType, isGeneric);
        }
    }
}
