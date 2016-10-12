package com.quancheng.boot.starter;

import java.util.concurrent.ExecutionException;

import org.lognet.springboot.grpc.proto.GreeterGrpc;
import org.lognet.springboot.grpc.proto.GreeterOuterClass;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcStubCall {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 12201).usePlaintext(true).build();
        String name = "John";
        final GreeterGrpc.GreeterFutureStub greeterFutureStub = GreeterGrpc.newFutureStub(channel);
        final GreeterOuterClass.HelloRequest helloRequest = GreeterOuterClass.HelloRequest.newBuilder().setName(name).build();
        final String reply = greeterFutureStub.sayHello(helloRequest).get().getMessage();
        System.out.println(reply);

    }

}
