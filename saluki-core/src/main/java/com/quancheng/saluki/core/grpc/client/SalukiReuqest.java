package com.quancheng.saluki.core.grpc.client;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.filter.GrpcRequest;
import com.quancheng.saluki.core.grpc.utils.MethodDescriptorUtils;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public final class SalukiReuqest {

    private final GrpcRequest request;

    public SalukiReuqest(GrpcRequest request){
        this.request = request;
    }

    public Message getRequestArg() {
        Object arg = request.getMethodRequest().getArg();
        return PojoProtobufUtils.Pojo2Protobuf(arg);
    }

    public MethodDescriptor<Message, Message> getMethodDescriptor() {
        Message argsReq = MethodDescriptorUtils.buildDefaultInstance(request.getMethodRequest().getRequestType());
        Message argsRep = MethodDescriptorUtils.buildDefaultInstance(request.getMethodRequest().getResponseType());
        return MethodDescriptorUtils.createMethodDescriptor(request.getServiceName(),
                                                            request.getMethodRequest().getMethodName(), argsReq,
                                                            argsRep);
    }

    public GrpcRequest getRequest() {
        return request;
    }

    public Channel getChannel() {
        return request.getCall().getChannel();
    }

}
