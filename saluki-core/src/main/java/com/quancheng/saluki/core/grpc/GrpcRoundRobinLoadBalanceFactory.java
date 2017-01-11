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
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.client.GrpcAsyncCall;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.grpc.router.GrpcRouter;
import com.quancheng.saluki.core.grpc.router.GrpcRouterFactory;

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
public class GrpcRoundRobinLoadBalanceFactory extends LoadBalancer.Factory {

    private static final GrpcRoundRobinLoadBalanceFactory instance = new GrpcRoundRobinLoadBalanceFactory();

    private GrpcRoundRobinLoadBalanceFactory(){
    }

    public static GrpcRoundRobinLoadBalanceFactory getInstance() {
        return instance;
    }

    @Override
    public <T> LoadBalancer<T> newLoadBalancer(String serviceName, TransportManager<T> tm) {
        return new RoundRobinLoadBalancer<T>(tm);
    }

    private static class RoundRobinLoadBalancer<T> extends LoadBalancer<T> {

        private static final Logger           log             = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);

        private static final Status           SHUTDOWN_STATUS = Status.UNAVAILABLE.augmentDescription("RoundRobinLoadBalancer has shut down");

        private final Object                  lock            = new Object();

        private final TransportManager<T>     tm;

        @GuardedBy("lock")
        private RoundRobinServerListExtend<T> addresses;

        @GuardedBy("lock")
        private InterimTransport<T>           interimTransport;

        @GuardedBy("lock")
        private Status                        nameResolutionError;

        @GuardedBy("lock")
        private boolean                       closed;

        private volatile Attributes           nameNameResolver_attributes;

        private volatile Attributes           clientInvoke_attributes;

        private RoundRobinLoadBalancer(TransportManager<T> tm){
            this.tm = tm;
        }

        @Override
        public T pickTransport(Attributes affinity) {
            this.clientInvoke_attributes = affinity;
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
            RoundRobinServerListExtend<T> addressCopyRouted = routerAddress(addressesCopy);
            T t = addressCopyRouted.getTransportForNextServer();
            this.doSaveRemoteInfo(addressCopyRouted);
            return t;
        }

        private RoundRobinServerListExtend<T> routerAddress(RoundRobinServerListExtend<T> addressesCopy) {
            GrpcURL refUrl = this.clientInvoke_attributes.get(GrpcAsyncCall.GRPC_REF_URL);
            String routerMessage = this.nameNameResolver_attributes.get(GrpcNameResolverProvider.GRPC_ROUTER_MESSAGE);
            if (StringUtils.isNotEmpty(routerMessage)) {
                GrpcRouter grpcRouter = GrpcRouterFactory.getInstance().createRouter(refUrl, routerMessage);
                List<SocketAddress> updatedServers = Lists.newArrayList();
                for (SocketAddress server : addressesCopy.getServers()) {
                    List<GrpcURL> providerUrls = findGrpcURLByAddress(server);
                    if (grpcRouter.match(providerUrls)) {
                        updatedServers.add(server);
                    }
                }
                if (updatedServers.isEmpty()) {
                    throw new IllegalArgumentException("The router condition has stoped all server address");
                } else {
                    RoundRobinServerListExtend.Builder<T> listBuilder = new RoundRobinServerListExtend.Builder<T>(tm);
                    for (SocketAddress server : updatedServers) {
                        listBuilder.add(server);
                    }
                    return listBuilder.build();
                }
            }
            return addressesCopy;
        }

        private List<GrpcURL> findGrpcURLByAddress(SocketAddress address) {
            Map<List<SocketAddress>, GrpcURL> addressMapping = this.nameNameResolver_attributes.get(GrpcNameResolverProvider.GRPC_ADDRESS_GRPCURL_MAPPING);
            List<GrpcURL> providerUrls = Lists.newArrayList();
            if (!addressMapping.isEmpty()) {
                for (Map.Entry<List<SocketAddress>, GrpcURL> entry : addressMapping.entrySet()) {
                    List<SocketAddress> allAddress = entry.getKey();
                    if (allAddress.contains(address)) {
                        providerUrls.add(entry.getValue());
                    }
                }
            }
            return providerUrls;
        }

        private void doSaveRemoteInfo(RoundRobinServerListExtend<T> serverList) {
            SocketAddress currentAddress = serverList.getCurrentServer();
            List<SocketAddress> addresses = serverList.getServers();
            HashMap<Key<?>, Object> data = new HashMap<Key<?>, Object>();
            NameResolver.Listener nameResolverListener = this.nameNameResolver_attributes.get(GrpcAsyncCall.NAMERESOVER_LISTENER);
            List<SocketAddress> remoteAddressList = this.nameNameResolver_attributes.get(GrpcAsyncCall.NOTPICKED_REMOTE_ADDR_KEYS);
            if (nameResolverListener != null) {
                data.put(GrpcAsyncCall.NAMERESOVER_LISTENER, nameResolverListener);
            }
            if (remoteAddressList != null) {
                data.put(GrpcAsyncCall.NOTPICKED_REMOTE_ADDR_KEYS, remoteAddressList);
            }
            if (currentAddress != null) {
                data.put(GrpcAsyncCall.REMOTE_ADDR_KEY, currentAddress);
            }
            if (addresses != null) {
                data.put(GrpcAsyncCall.PICKED_REMOTE_ADDR_KEYS, addresses);
            }
            try {
                Class<?> classType = clientInvoke_attributes.getClass();
                Field field = classType.getDeclaredField("data");
                field.setAccessible(true);
                field.set(clientInvoke_attributes, data);
            } catch (Exception e) {
                RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
                throw rpcFramwork;
            }
        }

        @Override
        public void handleResolvedAddresses(List<? extends List<ResolvedServerInfo>> updatedServers,
                                            Attributes config) {
            try {
                this.nameNameResolver_attributes = config;
                final InterimTransport<T> savedInterimTransport;
                final RoundRobinServerListExtend<T> addressesCopy;
                synchronized (lock) {
                    if (closed) {
                        return;
                    }
                    addresses = createRoundRobinServer(updatedServers);
                    addressesCopy = addresses;
                    nameResolutionError = null;
                    savedInterimTransport = interimTransport;
                    interimTransport = null;
                }
                if (savedInterimTransport != null) {
                    savedInterimTransport.closeWithRealTransports(new Supplier<T>() {

                        @Override
                        public T get() {
                            RoundRobinServerListExtend<T> addressCopyRouted = RoundRobinLoadBalancer.this.routerAddress(addressesCopy);
                            T t = addressCopyRouted.getTransportForNextServer();
                            RoundRobinLoadBalancer.this.doSaveRemoteInfo(addressCopyRouted);
                            return t;
                        }
                    });
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }

        }

        private RoundRobinServerListExtend<T> createRoundRobinServer(List<? extends List<ResolvedServerInfo>> updatedServers) {
            RoundRobinServerListExtend.Builder<T> listBuilder = new RoundRobinServerListExtend.Builder<T>(tm);
            for (List<ResolvedServerInfo> servers : updatedServers) {
                if (servers.isEmpty()) {
                    continue;
                }
                for (ResolvedServerInfo server : servers) {
                    listBuilder.add(server.getAddress());
                }
            }
            return listBuilder.build();
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
