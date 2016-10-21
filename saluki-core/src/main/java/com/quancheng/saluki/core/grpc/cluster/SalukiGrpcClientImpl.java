package com.quancheng.saluki.core.grpc.cluster;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import com.google.common.base.Predicates;
import com.google.common.util.concurrent.Futures;
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

    private RetryOptions             retryOptions;
    private ScheduledExecutorService retryExecutorService;
    private CallOptionsFactory       callOptionsFactory = new CallOptionsFactory.Default();

    private SalukiAsyncUtilities     asyncUtilities;

    SalukiGrpcClientImpl(ChannelPool channelPool, ScheduledExecutorService retryExecutorService,
                         SalukiAsyncUtilities asyncUtilities){

    }

    public ListenableFuture<List<Message>> streamingFuture(Message request, MethodDescriptor<Message, Message> method) {
        SalukiAsyncRpc<Message, Message> asyncRpc = asyncUtilities.createAsyncRpc(method,
                                                                                  Predicates.<Message> alwaysTrue());
        return null;
        // return Futures.transform(getStreamingFuture(request, asyncRpc));
    }

    @Override
    public <ReqT, RespT> ListenableFuture<List<RespT>> getStreamingFuture(ReqT request,
                                                                          SalukiAsyncRpc<ReqT, RespT> rpc) {
        return getCompletionFuture(createStreamingListener(request, rpc));
    }

    @Override
    public <ReqT, RespT> List<RespT> getBlockingStreamingResult(ReqT request, SalukiAsyncRpc<ReqT, RespT> rpc) {
        return getBlockingResult(createStreamingListener(request, rpc));
    }

    @Override
    public <ReqT, RespT> ListenableFuture<RespT> getUnaryFuture(ReqT request, SalukiAsyncRpc<ReqT, RespT> rpc,
                                                                int retryTimes) {
        return getCompletionFuture(createUnaryListener(request, rpc));
    }

    @Override
    public <ReqT, RespT> RespT getBlockingUnaryResult(ReqT request, SalukiAsyncRpc<ReqT, RespT> rpc) {
        return getBlockingResult(createUnaryListener(request, rpc));
    }

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
        return callOptionsFactory.create(methodDescriptor, request);
    }

    private static <ReqT, RespT, OutputT> ListenableFuture<OutputT> getCompletionFuture(AbstractRetryingRpcListener<ReqT, RespT, OutputT> listener) {
        listener.start();
        return listener.getCompletionFuture();
    }

    private static <ReqT, RespT, OutputT> OutputT getBlockingResult(AbstractRetryingRpcListener<ReqT, RespT, OutputT> listener) {
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
