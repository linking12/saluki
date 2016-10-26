package com.quancheng.saluki.core.grpc;

import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.base.Supplier;
import com.quancheng.saluki.core.grpc.client.ha.internal.CallOptionsFactory;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;

import io.grpc.Attributes;
import io.grpc.Attributes.Key;
import io.grpc.LoadBalancer;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;
import io.grpc.TransportManager;
import io.grpc.TransportManager.InterimTransport;

public class SalukiRoundRobinLoadBalanceFactory extends LoadBalancer.Factory {

    private static final SalukiRoundRobinLoadBalanceFactory instance = new SalukiRoundRobinLoadBalanceFactory();

    private SalukiRoundRobinLoadBalanceFactory(){
    }

    public static SalukiRoundRobinLoadBalanceFactory getInstance() {
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
        private volatile Attributes           nameResolverConfig;

        private final TransportManager<T>     tm;

        private RoundRobinLoadBalancer(TransportManager<T> tm){
            this.tm = tm;
        }

        @Override
        public T pickTransport(Attributes affinity) {
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
            this.saveAddress(affinity, addressesCopy);
            return t;
        }

        private void saveAddress(Attributes affinity, RoundRobinServerListExtend<T> serverList) {
            SocketAddress currentAddress = serverList.getCurrentServer();
            List<SocketAddress> addresses = serverList.getServers();
            NameResolver.Listener listener = this.nameResolverConfig.get(CallOptionsFactory.NAMERESOVER_LISTENER);
            List<SocketAddress> registryaddresses = this.nameResolverConfig.get(CallOptionsFactory.REMOTE_ADDR_KEYS_REGISTRY);
            HashMap<Key<?>, Object> data = new HashMap<Key<?>, Object>();
            if (listener != null) {
                data.put(CallOptionsFactory.NAMERESOVER_LISTENER, listener);
            }
            if (currentAddress != null) {
                data.put(CallOptionsFactory.REMOTE_ADDR_KEY, currentAddress);
            }
            if (addresses != null) {
                data.put(CallOptionsFactory.REMOTE_ADDR_KEYS, addresses);
            }
            if (registryaddresses != null) {
                data.put(CallOptionsFactory.REMOTE_ADDR_KEYS_REGISTRY, registryaddresses);
            }
            fillData(affinity, data);
        }

        /**
         * 这里有点比较low，由于affinity是保护的，没法覆盖值，所以只能用反射来强制设置值 这里需要看看能否有优化之处
         */
        private void fillData(Attributes affinity, HashMap<Key<?>, Object> data) {
            try {
                Class<?> classType = affinity.getClass();
                Field field = classType.getDeclaredField("data");
                field.setAccessible(true);
                field.set(affinity, data);
            } catch (Exception e) {
                RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
                throw rpcFramwork;
            }
        }

        @Override
        public void handleResolvedAddresses(List<? extends List<ResolvedServerInfo>> updatedServers,
                                            Attributes config) {
            this.nameResolverConfig = config;
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
                        return addressesCopy.getTransportForNextServer();
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
