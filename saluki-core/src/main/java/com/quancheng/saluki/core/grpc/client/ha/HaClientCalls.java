package com.quancheng.saluki.core.grpc.client.ha;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Predicates;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.client.ha.internal.AbstractRetryingRpcListener;
import com.quancheng.saluki.core.grpc.client.ha.internal.CallOptionsFactory;
import com.quancheng.saluki.core.grpc.client.ha.internal.RetryingCollectingClientCallListener;
import com.quancheng.saluki.core.grpc.client.ha.internal.RetryingUnaryRpcCallListener;
import com.quancheng.saluki.core.grpc.monitor.MonitorService;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

public interface HaClientCalls {

    public ListenableFuture<List<Message>> streamingFuture(Message request, MethodDescriptor<Message, Message> method);

    public ListenableFuture<Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method);

    public List<Message> blockingStreamResult(Message request, MethodDescriptor<Message, Message> method);

    public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method);

    public static class Default implements HaClientCalls {

        private final Channel      channel;

        private final RetryOptions retryOptions;

        public Default(Channel channel, RetryOptions retryOptions){
            this.channel = channel;
            this.retryOptions = retryOptions;
        }

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

        /**
         * Help Method
         */
        private HaAsyncRpc<Message, Message> buildAsyncRpc(MethodDescriptor<Message, Message> method) {
            HaAsyncUtilities asyncUtilities = new HaAsyncUtilities.Default(channel);
            HaAsyncRpc<Message, Message> asyncRpc = asyncUtilities.createAsyncRpc(method,
                                                                                  Predicates.<Message> alwaysTrue());
            return asyncRpc;
        }

        private <ReqT, RespT> RetryingCollectingClientCallListener<ReqT, RespT> createStreamingListener(ReqT request,
                                                                                                        HaAsyncRpc<ReqT, RespT> rpc) {
            CallOptions callOptions = getCallOptions(rpc.getMethodDescriptor(), request);
            SocketAddress remote = callOptions.getAffinity().get(CallOptionsFactory.REMOTE_ADDR_KEY);
            if (remote != null) {
                InetSocketAddress remoteAddress = (InetSocketAddress) remote;
                RpcContext.getContext().setAttachment(MonitorService.PROVIDER,
                                                      remoteAddress.getAddress().getHostAddress());
            }
            return new RetryingCollectingClientCallListener<>(retryOptions, request, rpc, callOptions,
                                                              createMetadata());
        }

        private <ReqT, RespT> RetryingUnaryRpcCallListener<ReqT, RespT> createUnaryListener(ReqT request,
                                                                                            HaAsyncRpc<ReqT, RespT> rpc) {
            CallOptions callOptions = getCallOptions(rpc.getMethodDescriptor(), request);
            return new RetryingUnaryRpcCallListener<>(retryOptions, request, rpc, callOptions, createMetadata());
        }

        private Metadata createMetadata() {
            Metadata metadata = new Metadata();
            return metadata;
        }

        private <ReqT> CallOptions getCallOptions(final MethodDescriptor<ReqT, ?> methodDescriptor, ReqT request) {
            CallOptionsFactory callOptionsFactory = new CallOptionsFactory.Default();
            return callOptionsFactory.create(methodDescriptor, request);
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

    }

}
