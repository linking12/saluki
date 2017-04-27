/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.interceptor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.util.MetadataKeyUtil;
import com.quancheng.saluki.core.grpc.util.SerializerUtils;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

/**
 * @author shimingliu 2016年12月14日 下午10:29:37
 * @version HeaderServerInterceptor.java, v 0.0.1 2016年12月14日 下午10:29:37 shimingliu
 */
public class HeaderServerInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HeaderServerInterceptor.class);

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, final Metadata headers,
                                                      ServerCallHandler<ReqT, RespT> next) {
        return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {

            @Override
            public void request(int numMessages) {
                InetSocketAddress remoteAddress = (InetSocketAddress) call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
                RpcContext.getContext().setAttachment(Constants.REMOTE_ADDRESS, remoteAddress.getHostString());
                copyMetadataToThreadLocal(headers);
                super.request(numMessages);
            }

            @Override
            public void close(Status status, Metadata trailers) {
                if (status.getCause() != null) {
                    StringWriter sw = new StringWriter();
                    status.getCause().printStackTrace(new PrintWriter(sw));
                    trailers.put(MetadataKeyUtil.GRPC_ERRORCAUSE_VALUE, sw.toString());
                }
                super.close(status, trailers);

            }
        }, headers);
    }

    private void copyMetadataToThreadLocal(Metadata headers) {
        String attachments = headers.get(MetadataKeyUtil.GRPC_CONTEXT_ATTACHMENTS);
        String values = headers.get(MetadataKeyUtil.GRPC_CONTEXT_VALUES);
        try {
            if (attachments != null) {
                Map<String, String> attachmentsMap = SerializerUtils.fromJson(attachments,
                                                                              new TypeToken<Map<String, String>>() {
                                                                              }.getType());
                RpcContext.getContext().setAttachments(attachmentsMap);
            }
            if (values != null) {
                Map<String, Object> valuesMap = SerializerUtils.fromJson(values, new TypeToken<Map<String, Object>>() {
                }.getType());
                for (Map.Entry<String, Object> entry : valuesMap.entrySet()) {
                    RpcContext.getContext().set(entry.getKey(), entry.getValue());
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }
}
