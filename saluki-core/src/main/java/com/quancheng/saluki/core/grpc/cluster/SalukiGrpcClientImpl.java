package com.quancheng.saluki.core.grpc.cluster;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import com.google.common.base.Predicates;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.cluster.async.AbstractRetryingRpcListener;
import com.quancheng.saluki.core.grpc.cluster.async.RetryingCollectingClientCallListener;
import com.quancheng.saluki.core.grpc.cluster.async.RetryingUnaryRpcCallListener;
import com.quancheng.saluki.core.grpc.cluster.async.SalukiAsyncRpc;
import com.quancheng.saluki.core.grpc.cluster.async.SalukiAsyncUtilities;
import com.quancheng.saluki.core.grpc.cluster.config.RetryOptions;
import com.quancheng.saluki.core.grpc.cluster.io.ChannelPool;

import io.grpc.CallOptions;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

public class SalukiGrpcClientImpl implements SalukiGrpcClient {

    private final ChannelPool              channelPool;

    private final RetryOptions             retryOptions;

    private final ScheduledExecutorService retryExecutorService;

    SalukiGrpcClientImpl(ChannelPool channelPool, ScheduledExecutorService retryExecutorService,
                         RetryOptions retryOptions){
        this.channelPool = channelPool;
        this.retryExecutorService = retryExecutorService;
        this.retryOptions = retryOptions;
    }

    @Override
    public ListenableFuture<List<Message>> streamingFuture(Message request, MethodDescriptor<Message, Message> method) {
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

    private SalukiAsyncRpc<Message, Message> buildAsyncRpc(MethodDescriptor<Message, Message> method) {
        SalukiAsyncUtilities asyncUtilities = new SalukiAsyncUtilities.Default(channelPool);
        SalukiAsyncRpc<Message, Message> asyncRpc = asyncUtilities.createAsyncRpc(method,
                                                                                  Predicates.<Message> alwaysTrue());
        return asyncRpc;
    }

    /**
     * Help Method
     */
    private <ReqT, RespT> RetryingCollectingClientCallListener<ReqT, RespT> createStreamingListener(ReqT request,
                                                                                                    SalukiAsyncRpc<ReqT, RespT> rpc) {
        CallOptions callOptions = getCallOptions(rpc.getMethodDescriptor(), request);
        return new RetryingCollectingClientCallListener<>(retryOptions, request, rpc, callOptions, retryExecutorService,
                                                          createMetadata());
    }

    private <ReqT, RespT> RetryingUnaryRpcCallListener<ReqT, RespT> createUnaryListener(ReqT request,
                                                                                        SalukiAsyncRpc<ReqT, RespT> rpc) {
        CallOptions callOptions = getCallOptions(rpc.getMethodDescriptor(), request);
        return new RetryingUnaryRpcCallListener<>(retryOptions, request, rpc, callOptions, retryExecutorService,
                                                  createMetadata());
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
    /**
     * Help Method
     */

}
