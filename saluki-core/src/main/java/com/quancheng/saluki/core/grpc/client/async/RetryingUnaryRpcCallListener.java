package com.quancheng.saluki.core.grpc.client.async;

import com.quancheng.saluki.core.grpc.client.async.AsyncCallInternal.AsyncCallClientInternal;

import io.grpc.CallOptions;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RetryingUnaryRpcCallListener<RequestT, ResponseT> extends AbstractRetryingRpcListener<RequestT, ResponseT, ResponseT> {

    final static StatusRuntimeException NO_VALUE_SET_EXCEPTION = Status.INTERNAL.withDescription("No value received for unary call").asRuntimeException();

    private ResponseT                   value;

    public RetryingUnaryRpcCallListener(RetryOptions retryOptions, RequestT request,
                                        AsyncCallClientInternal<RequestT, ResponseT> retryableRpc,
                                        CallOptions callOptions, Metadata metadata){
        super(retryOptions, request, retryableRpc, callOptions, metadata);
    }

    @Override
    public void onMessage(ResponseT message) {
        value = message;
        completionFuture.set(value);
    }

    @Override
    protected void onOK() {
        if (value == null) {
            completionFuture.setException(NO_VALUE_SET_EXCEPTION);
        }
    }
}
