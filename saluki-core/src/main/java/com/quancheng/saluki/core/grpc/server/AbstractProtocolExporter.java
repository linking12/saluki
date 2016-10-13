package com.quancheng.saluki.core.grpc.server;

import java.lang.reflect.Method;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.grpc.MethodDescriptorUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.stub.StreamObserver;
import io.grpc.stub.ServerCalls.UnaryMethod;

public abstract class AbstractProtocolExporter implements ProtocolExporter {

    private final Class<?> protocol;

    private final Object   protocolImpl;

    public AbstractProtocolExporter(Class<?> protocol, Object protocolImpl){
        this.protocol = protocol;
        this.protocolImpl = protocolImpl;
    }

    public Class<?> getProtocol() {
        return protocol;
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
                /**
                 * 对入参进行转化，pb model --->pojo begin
                 */
                Class<?> requestType = ReflectUtil.getTypedReq(method);
                Object req = MethodDescriptorUtils.convertPbModelToPojo(request, requestType);
                /**
                 * 对入参进行转化，pb model --->pojo end
                 */
                Object[] requestParams = new Object[] { req };
                Object response = method.invoke(serviceToInvoke, requestParams);

                /**
                 * 对出参进行转化，pojo ---> pb model
                 */
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
