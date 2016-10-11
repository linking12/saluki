package com.quancheng.saluki.core.grpc.server;

import java.lang.reflect.Method;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.grpc.MethodDescriptorUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;

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
                Object request_ = MethodDescriptorUtils.convertPbModelToPojo(request, ReflectUtil.getTypedReq(method));
                Object[] requestParams = new Object[] { request_ };
                Object response = method.invoke(serviceToInvoke, requestParams);
                GeneratedMessageV3 returnObj = (GeneratedMessageV3) MethodDescriptorUtils.convertPojoToPbModel(response);
                responseObserver.onNext(returnObj);
            } catch (Exception ex) {
                responseObserver.onError(ex);
            } finally {
                responseObserver.onCompleted();
            }
        }

    }
}
