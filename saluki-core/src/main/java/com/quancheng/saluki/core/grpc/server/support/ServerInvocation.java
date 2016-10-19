package com.quancheng.saluki.core.grpc.server.support;

import java.lang.reflect.Method;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.stub.ServerCalls.UnaryMethod;
import io.grpc.stub.StreamObserver;

public class ServerInvocation implements UnaryMethod<Message, Message> {

    private final Object serviceToInvoke;
    private final Method method;

    public ServerInvocation(Object serviceToInvoke, Method method){
        this.serviceToInvoke = serviceToInvoke;
        this.method = method;
    }

    @Override
    public void invoke(Message request, StreamObserver<Message> responseObserver) {

        try {
            Class<?> requestType = ReflectUtil.getTypedReq(method);
            Object req = PojoProtobufUtils.Protobuf2Pojo(request, requestType);
            Object[] requestParams = new Object[] { req };
            Object response = method.invoke(serviceToInvoke, requestParams);
            Message message = PojoProtobufUtils.Pojo2Protobuf(response);
            responseObserver.onNext(message);
        } catch (Throwable ex) {
            responseObserver.onError(ex);
        } finally {
            responseObserver.onCompleted();
        }

    }

}
