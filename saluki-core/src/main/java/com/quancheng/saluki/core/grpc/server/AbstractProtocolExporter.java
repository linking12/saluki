package com.quancheng.saluki.core.grpc.server;

import java.lang.reflect.Method;

import com.google.protobuf.GeneratedMessageV3;

import io.grpc.stub.StreamObserver;
import io.grpc.stub.ServerCalls.UnaryMethod;

public abstract class AbstractProtocolExporter implements ProtocolExporter {

    private final Class<?> protocolClass;

    private final Object   protocolImpl;

    public AbstractProtocolExporter(Class<?> protocolClass, Object protocolImpl){
        this.protocolClass = protocolClass;
        this.protocolImpl = protocolImpl;
    }

    public Class<?> getProtocolClass() {
        return protocolClass;
    }

    public Object getProtocolImpl() {
        return protocolImpl;
    }

    protected class MethodInvokation implements UnaryMethod<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> {

        private final Object serviceToInvoke;
        private final Method method;

        public MethodInvokation(Object serviceToInvoke, Method method){
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
}
