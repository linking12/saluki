package com.quancheng.saluki.core.grpc.client;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.filter.GrpcRequest;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;
import com.quancheng.saluki.serializer.ProtobufEntity;

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
        Message argsReq = buildDefaultInstance(request.getMethodRequest().getRequestType());
        Message argsRep = buildDefaultInstance(request.getMethodRequest().getResponseType());
        return MethodDescriptor.create(MethodDescriptor.MethodType.UNARY,
                                       MethodDescriptor.generateFullMethodName(request.getServiceName(),
                                                                               request.getMethodRequest().getMethodName()),
                                       io.grpc.protobuf.ProtoUtils.marshaller(argsReq),
                                       io.grpc.protobuf.ProtoUtils.marshaller(argsRep));
    }

    public GrpcRequest getRequest() {
        return request;
    }

    public Channel getChannel() {
        return request.getCall().getChannel();
    }

    private Message buildDefaultInstance(Class<?> type) {
        Class<? extends Message> messageType;
        if (!Message.class.isAssignableFrom(type)) {
            ProtobufEntity entity = (ProtobufEntity) ReflectUtil.findAnnotation(type, ProtobufEntity.class);
            messageType = entity.value();
        } else {
            messageType = (Class<? extends Message>) type;
        }
        Object obj = ReflectUtil.classInstance(messageType);
        return (Message) obj;
    }

}
