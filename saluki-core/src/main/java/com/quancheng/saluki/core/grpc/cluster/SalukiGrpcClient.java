package com.quancheng.saluki.core.grpc.cluster;

import java.util.List;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;

import io.grpc.MethodDescriptor;

public interface SalukiGrpcClient {

    public ListenableFuture<List<Message>> streamingFuture(Message request, MethodDescriptor<Message, Message> method);

    public ListenableFuture<Message> unaryFuture(Message request, MethodDescriptor<Message, Message> method);

    public List<Message> blockingStreamResult(Message request, MethodDescriptor<Message, Message> method);

    public Message blockingUnaryResult(Message request, MethodDescriptor<Message, Message> method);

}
