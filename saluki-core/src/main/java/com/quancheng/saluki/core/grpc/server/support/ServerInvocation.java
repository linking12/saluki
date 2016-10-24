package com.quancheng.saluki.core.grpc.server.support;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.grpc.SalukiException;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCalls.UnaryMethod;
import io.grpc.stub.StreamObserver;

public class ServerInvocation implements UnaryMethod<Message, Message> {

    private static final Logger log = LoggerFactory.getLogger(ServerInvocation.class);

    private final Object        serviceToInvoke;
    private final Method        method;

    public ServerInvocation(Object serviceToInvoke, Method method){
        this.serviceToInvoke = serviceToInvoke;
        this.method = method;
    }

    @Override
    public void invoke(Message request, StreamObserver<Message> responseObserver) {
        try {
            String remoteAddress = RpcContext.getContext().getAttachment(SalukiConstants.REMOTE_ADDRESS);
            log.info(String.format("receiver %s request from %s", new Gson().toJson(request), remoteAddress));
            Class<?> requestType = ReflectUtil.getTypedReq(method);
            Object req = PojoProtobufUtils.Protobuf2Pojo(request, requestType);
            Object[] requestParams = new Object[] { req };
            Object response;
            try {
                response = method.invoke(serviceToInvoke, requestParams);
            } catch (Throwable e) {
                SalukiException exception = new SalukiException(SalukiException.BIZ_EXCEPTION, e);
                throw exception;
            }
            Message message = PojoProtobufUtils.Pojo2Protobuf(response);
            responseObserver.onNext(message);
            responseObserver.onCompleted();
        } catch (Throwable e) {
            SalukiException exception;
            if (!(e instanceof SalukiException)) {
                exception = new SalukiException(SalukiException.FRAMEWORK_EXCETPION, e);
            } else {
                exception = (SalukiException) e;
            }
            Metadata trailers = new Metadata(e.getMessage().getBytes(), exception2String(exception).getBytes());
            StatusRuntimeException statusException = new StatusRuntimeException(Status.INTERNAL, trailers);
            //log.error("invode service " + serviceToInvoke + " the method: " + method + " failed", statusException);
            responseObserver.onError(statusException);
        }
    }

    private String exception2String(Throwable e) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(": " + e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

}
