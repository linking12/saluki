package com.quancheng.saluki.core.grpc.client.async;

import com.quancheng.saluki.core.common.RpcContext;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientCall.Listener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public interface AsyncCallInternal {

    public static interface AsyncCallClientInternal<REQUEST, RESPONSE> {

        public ClientCall<REQUEST, RESPONSE> newCall(CallOptions callOptions);

        public void start(ClientCall<REQUEST, RESPONSE> call, REQUEST request, ClientCall.Listener<RESPONSE> listener,
                          Metadata metadata);

    }

    public static <RequestT, ResponseT> AsyncCallClientInternal<RequestT, ResponseT> createGrpcAsyncCall(final Channel channel,
                                                                                                         final MethodDescriptor<RequestT, ResponseT> method) {

        return new AsyncCallClientInternal<RequestT, ResponseT>() {

            @Override
            public ClientCall<RequestT, ResponseT> newCall(CallOptions callOptions) {
                return channel.newCall(method, callOptions);
            }

            @Override
            public void start(ClientCall<RequestT, ResponseT> call, RequestT request, Listener<ResponseT> listener,
                              Metadata metadata) {
                RpcContext.getContext().removeAttachment("routerRule");
                call.start(listener, metadata);
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
