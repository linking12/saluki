package com.quancheng.saluki.core.grpc;

import java.util.Set;

import com.google.common.collect.Sets;

import io.grpc.Server;

public class SalukiServer {

    private static final Set<Server> servers = Sets.newConcurrentHashSet();

    public SalukiServer(Server... args){
        for (Server server : args) {
            servers.add(server);
        }
    }

    public void awaitTermination() throws InterruptedException {
        for (Server server : servers) {
            server.awaitTermination();
        }
    }

}
