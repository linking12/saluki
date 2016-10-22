package com.quancheng.saluki.core.grpc.ha.async;

import java.util.concurrent.ScheduledExecutorService;

import com.quancheng.saluki.core.grpc.ha.RetryOptions;

import io.grpc.CallOptions;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RetryingUnaryRpcCallListener<RequestT, ResponseT> extends AbstractRetryingRpcListener<RequestT, ResponseT, ResponseT> {

    final static StatusRuntimeException NO_VALUE_SET_EXCEPTION = Status.INTERNAL.withDescription("No value received for unary call").asRuntimeException();

    private ResponseT                   value;

    public RetryingUnaryRpcCallListener(RetryOptions retryOptions, RequestT request,
                                        SalukiAsyncRpc<RequestT, ResponseT> retryableRpc, CallOptions callOptions,
                                        ScheduledExecutorService executorService, Metadata metadata){
        super(retryOptions, request, retryableRpc, callOptions, executorService, metadata);
    }

    @Override
    public void onMessage(ResponseT message) {
        value = message;
        completionFuture.set(value);
    }

    @Override
    protected void onOK() {
        if (value == null) {
            // No value received so mark the future as an error
            completionFuture.setException(NO_VALUE_SET_EXCEPTION);
        }
    }
}
