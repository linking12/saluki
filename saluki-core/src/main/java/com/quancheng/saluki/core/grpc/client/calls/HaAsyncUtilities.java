package com.quancheng.saluki.core.grpc.client.calls;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientCall.Listener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public interface HaAsyncUtilities {

    <RequestT, ResponseT> HaAsyncRpc<RequestT, ResponseT> createAsyncRpc(MethodDescriptor<RequestT, ResponseT> method,
                                                                             Predicate<RequestT> isRetryable);

    public static class Default implements HaAsyncUtilities {

        private final Channel channel;

        public Default(Channel channel){
            this.channel = channel;
        }

        @Override
        public <RequestT, ResponseT> HaAsyncRpc<RequestT, ResponseT> createAsyncRpc(final MethodDescriptor<RequestT, ResponseT> method,
                                                                                        final Predicate<RequestT> isRetryable) {
            return new HaAsyncRpc<RequestT, ResponseT>() {

                @Override
                public boolean isRetryable(RequestT request) {
                    return isRetryable.apply(request);
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
                        throw Throwables.propagate(t);
                    }
                    try {
                        call.halfClose();
                    } catch (Throwable t) {
                        call.cancel("Exception in halfClose.", t);
                        throw Throwables.propagate(t);
                    }
                }
            };
        }
    }
}
