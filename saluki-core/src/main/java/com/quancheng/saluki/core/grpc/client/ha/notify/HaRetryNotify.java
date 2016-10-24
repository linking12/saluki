package com.quancheng.saluki.core.grpc.client.ha.notify;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.quancheng.saluki.core.grpc.client.ha.internal.CallOptionsFactory;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;

public class HaRetryNotify {

    private final List<SocketAddress>   servers;

    private final SocketAddress         currentServer;

    private final NameResolver.Listener listener;

    public HaRetryNotify(Attributes affinity){
        this.currentServer = affinity.get(CallOptionsFactory.REMOTE_ADDR_KEY);
        this.servers = affinity.get(CallOptionsFactory.REMOTE_ADDR_KEYS);
        this.listener = affinity.get(CallOptionsFactory.NAMERESOVER_LISTENER);
    }

    public void onRefreshChannel() {
        List<SocketAddress> serversCopy = Lists.newArrayList();
        if (listener != null && currentServer != null && servers != null) {
            InetSocketAddress currentSock = (InetSocketAddress) currentServer;
            int serverSize = servers.size();
            if (serverSize > 2) {
                for (int i = 0; i < serverSize; i++) {
                    InetSocketAddress inetSock = (InetSocketAddress) servers.get(i);
                    if (!inetSock.getHostName().equals(currentSock.getHostName())) {
                        serversCopy.add(inetSock);
                    }
                }
            } else {
                serversCopy.addAll(servers);
            }
            notifyChannel(serversCopy);
        }
    }

    public void resetChannel() {
        if (servers != null) {
            notifyChannel(servers);
        }
    }

    private void notifyChannel(List<SocketAddress> servers) {
        if (listener != null) {
            List<ResolvedServerInfo> resolvedServers = new ArrayList<ResolvedServerInfo>(servers.size());
            Attributes config = Attributes.newBuilder().set(CallOptionsFactory.NAMERESOVER_LISTENER, listener).build();
            for (SocketAddress sock : servers) {
                ResolvedServerInfo serverInfo = new ResolvedServerInfo(sock, config);
                resolvedServers.add(serverInfo);
            }
            listener.onUpdate(Collections.singletonList(resolvedServers), config);
        }
    }
}
