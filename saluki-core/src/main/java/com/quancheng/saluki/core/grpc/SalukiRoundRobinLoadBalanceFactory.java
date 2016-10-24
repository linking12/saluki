package com.quancheng.saluki.core.grpc;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.base.Supplier;
import com.quancheng.saluki.core.grpc.client.ha.CallOptionsFactory;

import io.grpc.Attributes;
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
        private Attributes                    nameResolverConfig;

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
            Attributes.Builder builder = Attributes.newBuilder();
            if (listener != null) {
                builder.set(CallOptionsFactory.NAMERESOVER_LISTENER, listener);
            }
            if (currentAddress != null) {
                builder.set(CallOptionsFactory.REMOTE_ADDR_KEY, currentAddress);
            }
            if (addresses != null) {
                builder.set(CallOptionsFactory.REMOTE_ADDR_KEYS, addresses);
            }
            affinity = builder.build();
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

                    final List<SocketAddress> socketAddresses = new ArrayList<SocketAddress>(servers.size());
                    for (ResolvedServerInfo server : servers) {
                        socketAddresses.add(server.getAddress());
                    }
                    listBuilder.addList(socketAddresses);
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
