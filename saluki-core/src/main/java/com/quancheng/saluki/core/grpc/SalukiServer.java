package com.quancheng.saluki.core.grpc;

import io.grpc.Server;

public class SalukiServer {

    private Server[] servers;

    public SalukiServer(Server... args){
        servers = args;
    }

    public void awaitTermination() throws InterruptedException {
        for (Server server : servers) {
            server.awaitTermination();
        }
    }

}
