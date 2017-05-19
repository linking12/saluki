/*
 * Copyright 1999-2012 DianRong.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.saluki.core.grpc.client.failover;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.quancheng.saluki.core.grpc.GrpcNameResolverProvider;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

/**
 * @author liushiming 2017年5月2日 下午12:45:20
 * @version $Id: NameResolverNotify.java, v 0.0.1 2017年5月2日 下午12:45:20 liushiming
 */
public class NameResolverNotify {

    private final List<SocketAddress>   registry_servers;

    private final SocketAddress         current_server;

    private final NameResolver.Listener listener;

    private final Attributes            affinity;

    public NameResolverNotify(Map<String, Object> affinity){
        this.current_server = (SocketAddress) affinity.get(GrpcClientCall.GRPC_CURRENT_ADDR_KEY);
        Attributes nameresoveCache = (Attributes) affinity.get(GrpcClientCall.GRPC_NAMERESOVER_ATTRIBUTES);
        this.registry_servers = nameresoveCache.get(GrpcNameResolverProvider.REMOTE_ADDR_KEYS);
        this.listener = nameresoveCache.get(GrpcNameResolverProvider.NAMERESOVER_LISTENER);
        this.affinity = nameresoveCache;
    }

    public void refreshChannel() {
        List<SocketAddress> serversCopy = Lists.newArrayList();
        if (listener != null && current_server != null && registry_servers != null) {
            InetSocketAddress currentSock = (InetSocketAddress) current_server;
            int serverSize = registry_servers.size();
            if (serverSize >= 2) {
                for (int i = 0; i < serverSize; i++) {
                    InetSocketAddress inetSock = (InetSocketAddress) registry_servers.get(i);
                    boolean hostequal = inetSock.getHostName().equals(currentSock.getHostName());
                    boolean portequal = inetSock.getPort() == currentSock.getPort();
                    if (!hostequal || !portequal) {
                        serversCopy.add(inetSock);
                    }
                }
            } else {
                serversCopy.addAll(registry_servers);
            }
            if (serversCopy.size() != 0) {
                notifyChannel(serversCopy);
            }
        }
    }

    public void resetChannel() {
        if (registry_servers != null) {
            notifyChannel(registry_servers);
        }
    }

    private void notifyChannel(List<SocketAddress> addresses) {
        if (listener != null && registry_servers != null) {
            EquivalentAddressGroup server = new EquivalentAddressGroup(addresses);
            listener.onAddresses(Collections.singletonList(server), affinity);
        }
    }

}
