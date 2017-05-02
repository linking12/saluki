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
package com.quancheng.saluki.core.grpc.client.async;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.ResolvedServerInfoGroup;

/**
 * <strong>描述：</strong>TODO 描述 <br>
 * <strong>功能：</strong><br>
 * <strong>使用场景：</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author liushiming 2017年5月2日 下午12:45:20
 * @version $Id: NameResolverNotify.java, v 0.0.1 2017年5月2日 下午12:45:20 liushiming Exp $
 */
public class NameResolverNotify {

    private final List<SocketAddress>   registry_servers;

    private final SocketAddress         current_server;

    private final NameResolver.Listener listener;

    private final Attributes            affinity;

    public NameResolverNotify(Attributes affinity){
        this.current_server = affinity.get(GrpcClientCall.CURRENT_ADDR_KEY);
        this.registry_servers = affinity.get(GrpcClientCall.REGISTRY_REMOTE_ADDR_KEYS);
        this.listener = affinity.get(GrpcClientCall.NAMERESOVER_LISTENER);
        this.affinity = affinity;
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

    private void notifyChannel(List<SocketAddress> servers) {
        if (listener != null && registry_servers != null) {
            List<ResolvedServerInfo> resolvedServers = new ArrayList<ResolvedServerInfo>(servers.size());
            for (SocketAddress sock : servers) {
                ResolvedServerInfo serverInfo = new ResolvedServerInfo(sock, affinity);
                resolvedServers.add(serverInfo);
            }
            ResolvedServerInfoGroup serversGroup = ResolvedServerInfoGroup.builder().addAll(resolvedServers).build();
            listener.onUpdate(Collections.singletonList(serversGroup), affinity);
        }
    }

}
