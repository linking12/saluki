package com.quancheng.saluki.core.grpc.client.ha;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public interface HaAsyncRpc<REQUEST, RESPONSE> {

    ClientCall<REQUEST, RESPONSE> newCall(CallOptions callOptions);

    void start(ClientCall<REQUEST, RESPONSE> call, REQUEST request, ClientCall.Listener<RESPONSE> listener,
               Metadata metadata);

    boolean isRetryable(REQUEST request);

    MethodDescriptor<REQUEST, RESPONSE> getMethodDescriptor();

}
