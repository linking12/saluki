package com.quancheng.saluki.core.grpc.client.async;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientCall.Listener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public interface AsyncCallInternal {

    public static interface AsyncCallClientInternal<REQUEST, RESPONSE> {

        ClientCall<REQUEST, RESPONSE> newCall(CallOptions callOptions);

        void start(ClientCall<REQUEST, RESPONSE> call, REQUEST request, ClientCall.Listener<RESPONSE> listener,
                   Metadata metadata);

        boolean isRetryable();

        MethodDescriptor<REQUEST, RESPONSE> getMethodDescriptor();

    }

    public static <RequestT, ResponseT> AsyncCallClientInternal<RequestT, ResponseT> createGrpcAsyncCall(final Channel channel,
                                                                                                         final MethodDescriptor<RequestT, ResponseT> method,
                                                                                                         final Boolean isRetryable) {

        return new AsyncCallClientInternal<RequestT, ResponseT>() {

            @Override
            public boolean isRetryable() {
                return isRetryable;
            }

            @Override
            public MethodDescriptor<RequestT, ResponseT> getMethodDescriptor() {
                return method;
            }

            @Override
            public ClientCall<RequestT, ResponseT> newCall(CallOptions callOptions) {
                return channel.newCall(method, callOptions);
            }

            @Override
            public void start(ClientCall<RequestT, ResponseT> call, RequestT request, Listener<ResponseT> listener,
                              Metadata metadata) {
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
