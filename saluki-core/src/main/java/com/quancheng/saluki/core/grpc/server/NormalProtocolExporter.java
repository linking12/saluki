package com.quancheng.saluki.core.grpc.server;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.grpc.GrpcUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;

public class NormalProtocolExporter extends AbstractProtocolExporter {

    private static final Logger log = LoggerFactory.getLogger(ProtocolExporter.class);

    public NormalProtocolExporter(Class<?> protocolClass, Object protocolImpl){
        super(protocolClass, protocolImpl);
    }

    @Override
    public ServerServiceDefinition doExport() {
        Class<?> protocolClass = getProtocolClass();
        Object protocolInstance = getProtocolImpl();
        String serviceName = GrpcUtils.generateServiceName(protocolClass);
        ServerServiceDefinition.Builder serviceDefBuilder = ServerServiceDefinition.builder(serviceName);
        List<Method> methods = ReflectUtil.findAllPublicMethods(protocolClass);
        if (methods.isEmpty()) {
            throw new IllegalStateException("protocolClass " + serviceName + " not have export method"
                                            + protocolClass.getClass());
        }
        for (Method method : methods) {
            MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> methodDescriptor = GrpcUtils.createMethodDescriptor(protocolClass,
                                                                                                                         method);
            serviceDefBuilder.addMethod(methodDescriptor,
                                        ServerCalls.asyncUnaryCall(new MethodInvokation(protocolInstance, method)));
        }
        log.info("'{}' service has been registered.", serviceName);
        return serviceDefBuilder.build();
    }

}
