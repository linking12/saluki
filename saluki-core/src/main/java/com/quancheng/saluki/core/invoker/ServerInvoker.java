package com.quancheng.saluki.core.invoker;

import java.lang.reflect.Method;

import com.google.protobuf.GeneratedMessageV3;

import io.grpc.stub.ServerCalls.UnaryMethod;
import io.grpc.stub.StreamObserver;

public class ServerInvoker implements UnaryMethod<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> {

    private final Object serviceToInvoke;
    private final Method method;

    public ServerInvoker(Object serviceToInvoke, Method method){
        this.serviceToInvoke = serviceToInvoke;
        this.method = method;
    }

    @Override
    public void invoke(GeneratedMessageV3 request, StreamObserver<GeneratedMessageV3> responseObserver) {
        try {
            Object[] requestParams = new Object[] { request };
            GeneratedMessageV3 returnObj = (GeneratedMessageV3) method.invoke(serviceToInvoke, requestParams);
            responseObserver.onNext(returnObj);
        } catch (Exception ex) {
            responseObserver.onError(ex);
        } finally {
            responseObserver.onCompleted();
        }
    }

}
