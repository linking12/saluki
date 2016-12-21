/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc;

import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.base.Supplier;
import com.quancheng.saluki.core.grpc.client.GrpcAsyncCall;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;

import io.grpc.Attributes;
import io.grpc.Attributes.Key;
import io.grpc.Internal;
import io.grpc.LoadBalancer;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;
import io.grpc.TransportManager;
import io.grpc.TransportManager.InterimTransport;

/**
 * @author shimingliu 2016年12月14日 下午5:31:11
 * @version ThrallRoundRobinLoadBalanceFactory1.java, v 0.0.1 2016年12月14日 下午5:31:11 shimingliu
 */

@Internal
public class ThrallRoundRobinLoadBalanceFactory extends LoadBalancer.Factory {

    private static final ThrallRoundRobinLoadBalanceFactory instance = new ThrallRoundRobinLoadBalanceFactory();

    private ThrallRoundRobinLoadBalanceFactory(){
    }

    public static ThrallRoundRobinLoadBalanceFactory getInstance() {
        return instance;
    }

    @Override
    public <T> LoadBalancer<T> newLoadBalancer(String serviceName, TransportManager<T> tm) {
        return new RoundRobinLoadBalancer<T>(tm);
    }

    private static class RoundRobinLoadBalancer<T> extends LoadBalancer<T> {

        private static final Status           SHUTDOWN_STATUS = Status.UNAVAILABLE.augmentDescription("RoundRobinLoadBalancer has shut down");

        private final Object                  lock            = new Object();
        @GuardedBy("lock")
        private RoundRobinServerListExtend<T> addresses;
        @GuardedBy("lock")
        private InterimTransport<T>           interimTransport;
        @GuardedBy("lock")
        private Status                        nameResolutionError;
        @GuardedBy("lock")
        private boolean                       closed;
        @GuardedBy("lock")
        private volatile Attributes           nameResolver_Config;
        @GuardedBy("lock")
        private volatile Attributes           callOptions_Affinity;

        private final TransportManager<T>     tm;

        private RoundRobinLoadBalancer(TransportManager<T> tm){
            this.tm = tm;
        }

        @Override
        public T pickTransport(Attributes affinity) {
            this.callOptions_Affinity = affinity;
            final RoundRobinServerListExtend<T> addressesCopy;
            synchronized (lock) {
                if (closed) {
                    return tm.createFailingTransport(SHUTDOWN_STATUS);
                }
                if (addresses == null) {
                    if (nameResolutionError != null) {
                        return tm.createFailingTransport(nameResolutionError);
                    }
                    if (interimTransport == null) {
                        interimTransport = tm.createInterimTransport();
                    }
                    return interimTransport.transport();
                }
                addressesCopy = addresses;
            }
            T t = addressesCopy.getTransportForNextServer();
            this.doSaveRemoteInfo(addressesCopy);
            return t;
        }

        private void doSaveRemoteInfo(RoundRobinServerListExtend<T> serverList) {
            SocketAddress currentAddress = serverList.getCurrentServer();
            List<SocketAddress> addresses = serverList.getServers();
            NameResolver.Listener listener = this.nameResolver_Config.get(GrpcAsyncCall.NAMERESOVER_LISTENER);
            List<SocketAddress> registryaddresses = this.nameResolver_Config.get(GrpcAsyncCall.REMOTE_ADDR_KEYS_REGISTRY);
            HashMap<Key<?>, Object> data = new HashMap<Key<?>, Object>();
            if (listener != null) {
                data.put(GrpcAsyncCall.NAMERESOVER_LISTENER, listener);
            }
            if (currentAddress != null) {
                data.put(GrpcAsyncCall.REMOTE_ADDR_KEY, currentAddress);
            }
            if (addresses != null) {
                data.put(GrpcAsyncCall.REMOTE_ADDR_KEYS, addresses);
            }
            if (registryaddresses != null) {
                data.put(GrpcAsyncCall.REMOTE_ADDR_KEYS_REGISTRY, registryaddresses);
            }
            /**
             * 这里有点比较low，由于affinity是保护的，没法覆盖值，所以只能用反射来强制设置值 这里需要看看能否有优化之处
             */
            try {
                Class<?> classType = callOptions_Affinity.getClass();
                Field field = classType.getDeclaredField("data");
                field.setAccessible(true);
                field.set(callOptions_Affinity, data);
            } catch (Exception e) {
                RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
                throw rpcFramwork;
            }
        }

        @Override
        public void handleResolvedAddresses(List<? extends List<ResolvedServerInfo>> updatedServers,
                                            Attributes config) {
            this.nameResolver_Config = config;
            final InterimTransport<T> savedInterimTransport;
            final RoundRobinServerListExtend<T> addressesCopy;
            synchronized (lock) {
                if (closed) {
                    return;
                }
                RoundRobinServerListExtend.Builder<T> listBuilder = new RoundRobinServerListExtend.Builder<T>(tm);
                for (List<ResolvedServerInfo> servers : updatedServers) {
                    if (servers.isEmpty()) {
                        continue;
                    }
                    for (ResolvedServerInfo server : servers) {
                        listBuilder.add(server.getAddress());
                    }
                }
                addresses = listBuilder.build();
                addressesCopy = addresses;
                nameResolutionError = null;
                savedInterimTransport = interimTransport;
                interimTransport = null;
            }
            if (savedInterimTransport != null) {
                savedInterimTransport.closeWithRealTransports(new Supplier<T>() {

                    @Override
                    public T get() {
                        T t = addressesCopy.getTransportForNextServer();
                        RoundRobinLoadBalancer.this.doSaveRemoteInfo(addressesCopy);
                        return t;

                    }
                });
            }
        }

        @Override
        public void handleNameResolutionError(Status error) {
            InterimTransport<T> savedInterimTransport;
            synchronized (lock) {
                if (closed) {
                    return;
                }
                error = error.augmentDescription("Name resolution failed");
                savedInterimTransport = interimTransport;
                interimTransport = null;
                nameResolutionError = error;
            }
            if (savedInterimTransport != null) {
                savedInterimTransport.closeWithError(error);
            }
        }

        @Override
        public void shutdown() {
            InterimTransport<T> savedInterimTransport;
            synchronized (lock) {
                if (closed) {
                    return;
                }
                closed = true;
                savedInterimTransport = interimTransport;
                interimTransport = null;
            }
            if (savedInterimTransport != null) {
                savedInterimTransport.closeWithError(SHUTDOWN_STATUS);
            }
        }
    }
}
