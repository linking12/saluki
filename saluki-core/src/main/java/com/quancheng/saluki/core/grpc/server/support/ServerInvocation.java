package com.quancheng.saluki.core.grpc.server.support;

import java.lang.reflect.Method;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;

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
        SocketAddress remoteAddress = (SocketAddress) RpcContext.getContext().get(SalukiConstants.REMOTE_ADDRESS);
        log.info(String.format("receiver %s request from %s", new Gson().toJson(request),
                               new Gson().toJson(remoteAddress)));
        try {
            Class<?> requestType = ReflectUtil.getTypedReq(method);
            Object req = PojoProtobufUtils.Protobuf2Pojo(request, requestType);
            Object[] requestParams = new Object[] { req };
            Object response = method.invoke(serviceToInvoke, requestParams);
            Message message = PojoProtobufUtils.Pojo2Protobuf(response);
            responseObserver.onNext(message);
        } catch (Throwable ex) {
            log.info(serviceToInvoke + "invoke " + method.getName() + "failed", ex);
            responseObserver.onError(ex);
        } finally {
            responseObserver.onCompleted();
        }

    }

}
