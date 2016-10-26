package com.quancheng.boot.starter;

import java.util.concurrent.ExecutionException;

import com.quancheng.examples.service.HelloServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcStubCall {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 12202).usePlaintext(true).build();
        String name = "John";
        final HelloServiceGrpc.HelloServiceFutureStub helloserviceFutureStub = HelloServiceGrpc.newFutureStub(channel);
        com.quancheng.examples.model.Hello.HelloRequest request = com.quancheng.examples.model.Hello.HelloRequest.newBuilder().setName(name).build();
        com.quancheng.examples.model.Hello.HelloReply reply = helloserviceFutureStub.sayHello(request).get();
        System.out.println(reply);

    }

}
