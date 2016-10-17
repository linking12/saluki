package com.quancheng.saluki.core.grpc.server;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.grpc.MethodDescriptorUtils;
import com.quancheng.saluki.core.grpc.ProtocolExporter;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;

public class DefaultProtocolExporter extends AbstractProtocolExporter {

    private static final Logger log = LoggerFactory.getLogger(ProtocolExporter.class);

    public DefaultProtocolExporter(Class<?> protocolClass, Object protocolImpl){
        super(protocolClass, protocolImpl);
    }

    @Override
    public ServerServiceDefinition doExport() {
        Class<?> serivce = getProtocol();
        Object serviceRef = getProtocolImpl();
        String serviceName = MethodDescriptorUtils.generateServiceName(serivce);
        ServerServiceDefinition.Builder serviceDefBuilder = ServerServiceDefinition.builder(serviceName);
        List<Method> methods = ReflectUtil.findAllPublicMethods(serivce);
        if (methods.isEmpty()) {
            throw new IllegalStateException("protocolClass " + serviceName + " not have export method"
                                            + serivce.getClass());
        }
        for (Method method : methods) {
            MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> methodDescriptor = MethodDescriptorUtils.createMethodDescriptor(serivce,
                                                                                                                                     method);
            serviceDefBuilder.addMethod(methodDescriptor,
                                        ServerCalls.asyncUnaryCall(new MethodInvokation(serviceRef, method)));
        }
        log.info("'{}' service has been registered.", serviceName);
        return serviceDefBuilder.build();
    }

}
