package com.quancheng.saluki.core.grpc.client.support;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Predicates;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.client.async.AbstractRetryingRpcListener;
import com.quancheng.saluki.core.grpc.client.async.AsyncCallInternal;
import com.quancheng.saluki.core.grpc.client.async.AsyncCallInternal.AsyncCallClientInternal;
import com.quancheng.saluki.core.grpc.client.async.RetryOptions;
import com.quancheng.saluki.core.grpc.client.async.RetryingCollectingClientCallListener;
import com.quancheng.saluki.core.grpc.client.async.RetryingUnaryRpcCallListener;
import com.quancheng.saluki.core.grpc.utils.MarshallersAttributesUtils;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

public interface GrpcAsyncCall {

    public ListenableFuture<List<Message>> streamingFuture(Message request, MethodDescriptor<Message, Message> method);

    public ListenableFuture<Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method);

    public List<Message> blockingStreamResult(Message request, MethodDescriptor<Message, Message> method);

    public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method);

    public SocketAddress getRemoteAddress();

    public static GrpcAsyncCall createGrpcAsyncCall(final Channel channel, final RetryOptions retryOptions) {
        return new GrpcAsyncCall() {

            private volatile CallOptions callOptions;

            @Override
            public ListenableFuture<List<Message>> streamingFuture(Message request,
                                                                   MethodDescriptor<Message, Message> method) {
                return getCompletionFuture(createStreamingListener(request, buildAsyncRpc(method)));
            }

            @Override
            public ListenableFuture<Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method) {
                return getCompletionFuture(createUnaryListener(request, buildAsyncRpc(method)));
            }

            @Override
            public List<Message> blockingStreamResult(Message request, MethodDescriptor<Message, Message> method) {
                return getBlockingResult(createStreamingListener(request, buildAsyncRpc(method)));
            }

            @Override
            public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method) {
                return getBlockingResult(createUnaryListener(request, buildAsyncRpc(method)));
            }

            @Override
            public SocketAddress getRemoteAddress() {
                return callOptions.getAffinity().get(MarshallersAttributesUtils.REMOTE_ADDR_KEY);
            }

            /**
             * Help Method
             */
            private AsyncCallClientInternal<Message, Message> buildAsyncRpc(MethodDescriptor<Message, Message> method) {
                AsyncCallClientInternal<Message, Message> asyncRpc = AsyncCallInternal.createGrpcAsyncCall(channel,
                                                                                                           method,
                                                                                                           Predicates.<Message> alwaysTrue());
                return asyncRpc;
            }

            private <ReqT, RespT> RetryingCollectingClientCallListener<ReqT, RespT> createStreamingListener(ReqT request,
                                                                                                            AsyncCallClientInternal<ReqT, RespT> rpc) {
                CallOptions callOptions = getCallOptions(rpc.getMethodDescriptor(), request);
                return new RetryingCollectingClientCallListener<>(retryOptions, request, rpc, callOptions,
                                                                  new Metadata());
            }

            private <ReqT, RespT> RetryingUnaryRpcCallListener<ReqT, RespT> createUnaryListener(ReqT request,
                                                                                                AsyncCallClientInternal<ReqT, RespT> rpc) {
                CallOptions callOptions = getCallOptions(rpc.getMethodDescriptor(), request);
                return new RetryingUnaryRpcCallListener<>(retryOptions, request, rpc, callOptions, new Metadata());
            }

            private <ReqT> CallOptions getCallOptions(final MethodDescriptor<ReqT, ?> methodDescriptor, ReqT request) {
                CallOptions callOptions = AsyncCallInternal.createCallOptions(methodDescriptor, request);
                this.callOptions = callOptions;
                return callOptions;
            }

            private <ReqT, RespT, OutputT> ListenableFuture<OutputT> getCompletionFuture(AbstractRetryingRpcListener<ReqT, RespT, OutputT> listener) {
                listener.start();
                return listener.getCompletionFuture();
            }

            private <ReqT, RespT, OutputT> OutputT getBlockingResult(AbstractRetryingRpcListener<ReqT, RespT, OutputT> listener) {
                try {
                    listener.start();
                    return listener.getCompletionFuture().get();
                } catch (InterruptedException e) {
                    listener.cancel();
                    throw Status.CANCELLED.withCause(e).asRuntimeException();
                } catch (ExecutionException e) {
                    listener.cancel();
                    throw Status.fromThrowable(e).asRuntimeException();
                }
            }
        };
    }

}
