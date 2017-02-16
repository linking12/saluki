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
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.client.GrpcAsyncCall;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.grpc.router.GrpcRouter;
import com.quancheng.saluki.core.grpc.router.GrpcRouterFactory;

import io.grpc.Attributes;
import io.grpc.Attributes.Key;
import io.grpc.Internal;
import io.grpc.LoadBalancer;
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

        @GuardedBy("lock")
        private GrpcRouter                    grpcRouter;

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
            T t = addressesCopy.getTransportForNextServer();
            this.mergeNameResolverAttribute2ClientInvokeAttribute(addressesCopy);
            return t;
        }

        private void mergeNameResolverAttribute2ClientInvokeAttribute(RoundRobinServerListExtend<T> serverList) {
            SocketAddress currentAddress = serverList.getCurrentServer();
            List<SocketAddress> addresses = serverList.getServers();
            HashMap<Key<?>, Object> data = new HashMap<Key<?>, Object>();
            for (Key<?> key : this.nameNameResolver_attributes.keys()) {
                Object obj = this.nameNameResolver_attributes.get(key);
                data.put(key, obj);
            }
            for (Key<?> key : this.clientInvoke_attributes.keys()) {
                Object obj = this.clientInvoke_attributes.get(key);
                data.put(key, obj);
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
                    createGrpcRouterByNameResolver(config);
                    // 如果有参数，说明不是第一次调用，refUrl是存在的
                    if (clientInvoke_attributes != null) {
                        addresses = RoundRobinLoadBalancer.this.routerAddress(addressesCopy);
                    }
                    nameResolutionError = null;
                    savedInterimTransport = interimTransport;
                    interimTransport = null;

                }
                if (savedInterimTransport != null) {
                    savedInterimTransport.closeWithRealTransports(new Supplier<T>() {

                        @Override
                        public T get() {
                            addresses = RoundRobinLoadBalancer.this.routerAddress(addressesCopy);
                            T t = addressesCopy.getTransportForNextServer();
                            RoundRobinLoadBalancer.this.mergeNameResolverAttribute2ClientInvokeAttribute(addressesCopy);
                            return t;
                        }
                    });
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }

        }

        /**
         * <pre>
         *  String routerMessage = "condition://host = 10.110.0.16 => host = 10.110.0.16";
         * <pre>
           String  routerMessage = "javascript://function route(refUrl,providerUrls) {"
                                   + "var result = false;"
                                   + "if(refUrl.host=='10.110.0.16'){"
                                   + "   for (i = 0; i < providerUrls.length; i ++) {"
                                   + "      if ('10.110.0.16' == providerUrls[i].host) {"
                                   + "        result = true;"
                                   + "      }else{"
                                   + "        allMatchThen = false;"
                                   + "        break;"
                                   + "      }"
                                   + "   }"
                                   + "}"
                                   + "return result;"
                                +"}" ;
         * </pre>
         */
        private RoundRobinServerListExtend<T> routerAddress(RoundRobinServerListExtend<T> addressesCopy) {
            GrpcURL refUrl = this.clientInvoke_attributes.get(GrpcAsyncCall.GRPC_REF_URL);
            synchronized (lock) {
                GrpcRouter grpcRouterCopy = grpcRouter;
                try {
                    String routerRule = RpcContext.getContext().getAttachment("routerRule");
                    if (routerRule != null) {
                        if (RpcContext.getContext().containAttachment("routerRule")) {
                            RpcContext.getContext().removeAttachment("routerRule");
                        }
                        grpcRouterCopy = GrpcRouterFactory.getInstance().createRouter(routerRule);
                    }
                } finally {
                    if (grpcRouterCopy != null) {
                        grpcRouterCopy.setRefUrl(refUrl);
                        List<SocketAddress> updatedServers = Lists.newArrayList();
                        for (SocketAddress server : addressesCopy.getServers()) {
                            List<GrpcURL> providerUrls = findGrpcURLByAddress(server);
                            if (grpcRouterCopy.match(providerUrls)) {
                                updatedServers.add(server);
                            }
                        }
                        if (updatedServers.isEmpty()) {
                            throw new IllegalArgumentException("The router condition has stoped all server address");
                        } else {
                            if (haveChanged(addressesCopy.getServers(), updatedServers)) {
                                RoundRobinServerListExtend.Builder<T> listBuilder = new RoundRobinServerListExtend.Builder<T>(tm);
                                for (SocketAddress server : updatedServers) {
                                    listBuilder.add(server);
                                }
                                return listBuilder.build();
                            } else {
                                return addressesCopy;
                            }
                        }
                    }
                }
            }
            return addressesCopy;
        }

        private boolean haveChanged(List<SocketAddress> newServers, List<SocketAddress> oldServers) {
            if (newServers == null | newServers.isEmpty()) {
                return false;
            } else if (oldServers != null) {
                boolean result = false;
                for (int i = 0; i < newServers.size(); i++) {
                    if (result) {
                        for (int j = 0; j < oldServers.size(); j++) {
                            if (newServers.get(i).equals(oldServers.get(j))) {
                                result = false;
                                break;
                            } else {
                                result = true;
                            }
                        }
                    }
                }
                return result;
            }
            return true;
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

        private void createGrpcRouterByNameResolver(Attributes config) {
            String routerMessage = config.get(GrpcNameResolverProvider.GRPC_ROUTER_MESSAGE);
            if (StringUtils.isNotEmpty(routerMessage)) {
                grpcRouter = GrpcRouterFactory.getInstance().createRouter(routerMessage);
            } else {
                grpcRouter = null;
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
