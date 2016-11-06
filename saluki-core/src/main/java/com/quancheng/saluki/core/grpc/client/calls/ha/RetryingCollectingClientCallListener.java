package com.quancheng.saluki.core.grpc.client.calls.ha;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.quancheng.saluki.core.grpc.client.calls.HaAsyncRpc;
import com.quancheng.saluki.core.grpc.client.calls.RetryOptions;

import io.grpc.CallOptions;
import io.grpc.Metadata;

public class RetryingCollectingClientCallListener<RequestT, ResponseT> extends AbstractRetryingRpcListener<RequestT, ResponseT, List<ResponseT>> {

    private ImmutableList.Builder<ResponseT> buffer;

    public RetryingCollectingClientCallListener(RetryOptions retryOptions, RequestT request,
                                                HaAsyncRpc<RequestT, ResponseT> retryableRpc, CallOptions callOptions,
                                                Metadata metadata){
        super(retryOptions, request, retryableRpc, callOptions, metadata);
    }

    @Override
    public void run() {
        buffer = new ImmutableList.Builder<>();
        super.run();
    }

    @Override
    public void onMessage(ResponseT message) {
        call.request(1);
        buffer.add(message);
    }

    @Override
    protected void onOK() {
        completionFuture.set(buffer.build());
    }
}
