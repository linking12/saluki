package com.quancheng.saluki.core.grpc.server.support;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.server.GrpcProtocolExporter;
import com.quancheng.saluki.core.grpc.utils.MethodDescriptorUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;

public class DefaultPolicyExporter implements GrpcProtocolExporter {

    private static final Logger log = LoggerFactory.getLogger(GrpcProtocolExporter.class);

    @Override
    public ServerServiceDefinition export(Class<?> protocol, Object protocolImpl) {
        Class<?> serivce = protocol;
        Object serviceRef = protocolImpl;
        String serviceName = protocol.getName();
        ServerServiceDefinition.Builder serviceDefBuilder = ServerServiceDefinition.builder(serviceName);
        List<Method> methods = ReflectUtil.findAllPublicMethods(serivce);
        if (methods.isEmpty()) {
            throw new IllegalStateException("protocolClass " + serviceName + " not have export method"
                                            + serivce.getClass());
        }
        for (Method method : methods) {
            MethodDescriptor<Message, Message> methodDescriptor = MethodDescriptorUtils.createMethodDescriptor(serivce,
                                                                                                               method);
            serviceDefBuilder.addMethod(methodDescriptor,
                                        ServerCalls.asyncUnaryCall(new ServerInvocation(serviceRef, method)));
        }
        log.info("'{}' service has been registered.", serviceName);
        return serviceDefBuilder.build();
    }

}
