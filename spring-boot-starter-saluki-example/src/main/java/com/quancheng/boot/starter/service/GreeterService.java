package com.quancheng.boot.starter.service;

import org.lognet.springboot.grpc.proto.GreeterOuterClass;

public interface GreeterService {

    public GreeterOuterClass.HelloReply SayHello(GreeterOuterClass.HelloRequest request);

}
