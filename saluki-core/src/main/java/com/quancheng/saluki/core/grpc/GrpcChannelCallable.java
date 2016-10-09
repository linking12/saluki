package com.quancheng.saluki.core.grpc;

import io.grpc.Channel;

public interface GrpcChannelCallable {

    public Channel getGrpcChannel(String serviceInterface);

}
