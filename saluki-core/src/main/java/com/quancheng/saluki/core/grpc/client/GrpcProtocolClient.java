package com.quancheng.saluki.core.grpc.client;

import io.grpc.Channel;

public interface GrpcProtocolClient<T> {

    public T getGrpcClient(ChannelCall channelCall, int callType, int callTimeout);

    public interface ChannelCall {

        public abstract Channel getChannel();
    }

}
