package com.quancheng.saluki.core.grpc.interceptor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.utils.Marshallers;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class HeaderClientInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HeaderClientInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions, Channel next) {
        return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                copyThreadLocalToMetadata(headers);
                super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {

                    @Override
                    public void onHeaders(Metadata headers) {
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }

    private void copyThreadLocalToMetadata(Metadata headers) {
        Map<String, String> attachments = RpcContext.getContext().getAttachments();
        Map<String, Object> values = RpcContext.getContext().get();
        try {
            if (!attachments.isEmpty()) {
                byte[] attachmentsBytes = new Gson().toJson(attachments).getBytes();
                headers.put(Marshallers.GRPC_CONTEXT_ATTACHMENTS, attachmentsBytes);
            }
            if (!values.isEmpty()) {
                byte[] attachmentsValues = new Gson().toJson(values).getBytes();
                headers.put(Marshallers.GRPC_CONTEXT_VALUES, attachmentsValues);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

}
