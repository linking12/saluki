package com.quancheng.saluki.core.invoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCalls;

public class ClientInvoker<T> implements InvocationHandler {

    private final ManagedChannel channel;

    private final Class<T>       protocol;

    private final int            callType;

    private final int            rpcTimeout;

    public ClientInvoker(Class<T> protocol, ManagedChannel channel, int rpcTimeout, int callType){
        this.channel = channel;
        this.protocol = protocol;
        this.callType = callType;
        this.rpcTimeout = rpcTimeout;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args.length > 1) {
            throw new IllegalArgumentException("grpc not support multiple args,args is " + args + " length is "
                                               + args.length);
        }
        io.grpc.MethodDescriptor<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> methodDescriptor = this.createMethodDescriptor(method);
        ClientCall<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> newCall = channel.newCall(methodDescriptor,
                                                                                                                             CallOptions.DEFAULT);
        com.google.protobuf.GeneratedMessageV3 arg = (com.google.protobuf.GeneratedMessageV3) args[0];
        switch (callType) {
            case 1:
                return ClientCalls.futureUnaryCall(newCall, arg).get(rpcTimeout, TimeUnit.SECONDS);
            case 2:
                return ClientCalls.blockingUnaryCall(newCall, arg);
            default:
                return ClientCalls.futureUnaryCall(newCall, arg).get(rpcTimeout, TimeUnit.SECONDS);
        }

    }

    private io.grpc.MethodDescriptor<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> createMethodDescriptor(Method method) {
        com.google.protobuf.GeneratedMessageV3 argsReq = (com.google.protobuf.GeneratedMessageV3) ReflectUtil.newMethodReq(method);
        com.google.protobuf.GeneratedMessageV3 argsRep = (com.google.protobuf.GeneratedMessageV3) ReflectUtil.newMethodRep(method);
        return io.grpc.MethodDescriptor.create(io.grpc.MethodDescriptor.MethodType.UNARY,
                                               generateFullMethodName(method.getName()),
                                               io.grpc.protobuf.ProtoUtils.marshaller(argsReq),
                                               io.grpc.protobuf.ProtoUtils.marshaller(argsRep));
    }

    private String generateFullMethodName(String methodName) {
        return protocol.getName() + "/" + methodName;
    }

}
