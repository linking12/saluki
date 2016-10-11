package com.quancheng.boot.starter.service;

import org.lognet.springboot.grpc.proto.GreeterOuterClass;
import org.lognet.springboot.grpc.proto.GreeterOuterClass.HelloReply;
import org.lognet.springboot.grpc.proto.GreeterOuterClass.HelloRequest;

import com.quancheng.boot.starter.saluki.GRpcService;

//@GRpcService(interfaceName = "com.quancheng.boot.starter.service.GreeterService", group = "default", version = "1.0.0")
public class GreeterServiceImpl implements GreeterService {

    @Override
    public HelloReply SayHello(HelloRequest request) {
        final GreeterOuterClass.HelloReply.Builder replyBuilder = GreeterOuterClass.HelloReply.newBuilder().setMessage("Hello "
                                                                                                                       + request.getName());
        return replyBuilder.build();
    }

}
