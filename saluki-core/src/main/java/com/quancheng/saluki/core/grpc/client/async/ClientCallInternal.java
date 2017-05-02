package com.quancheng.saluki.core.grpc.client.async;

import com.quancheng.saluki.core.common.RpcContext;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientCall.Listener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public interface ClientCallInternal<Request, Response> {

    public ClientCall<Request, Response> newCall(CallOptions callOptions);

    public void start(ClientCall<Request, Response> call, Request request, ClientCall.Listener<Response> listener);

    public static <Request, Response> ClientCallInternal<Request, Response> create(final Channel channel,
                                                                                   final MethodDescriptor<Request, Response> method) {

        return new ClientCallInternal<Request, Response>() {

            @Override
            public ClientCall<Request, Response> newCall(CallOptions callOptions) {
                return channel.newCall(method, callOptions);
            }

            @Override
            public void start(ClientCall<Request, Response> call, Request request, Listener<Response> listener) {
                RpcContext.getContext().removeAttachment("routerRule");
                call.start(listener, new Metadata());
                call.request(1);
                try {
                    call.sendMessage(request);
                } catch (Throwable t) {
                    call.cancel("Exception in sendMessage.", t);
                    throw t;
                }
                try {
                    call.halfClose();
                } catch (Throwable t) {
                    call.cancel("Exception in halfClose.", t);
                    throw t;
                }
            }

        };
    }
}
