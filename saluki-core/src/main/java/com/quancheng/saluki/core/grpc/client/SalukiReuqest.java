package com.quancheng.saluki.core.grpc.client;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.filter.GrpcRequest;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;
import com.quancheng.saluki.serializer.ProtobufEntity;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public final class SalukiReuqest {

    private GrpcRequest                  request;

    private final Cache<String, Channel> channelCache;

    public SalukiReuqest(){
        this.channelCache = CacheBuilder.newBuilder()//
                                        .maximumSize(10L)//
                                        .softValues()//
                                        .ticker(Ticker.systemTicker())//
                                        .build();
    }

    public void setRequest(GrpcRequest request) {
        this.request = request;
    }

    public GrpcRequest getRequest() {
        return request;
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

    public Channel getChannel() {
        try {
            return channelCache.get(request.getServiceName(), new Callable<Channel>() {

                @Override
                public Channel call() throws Exception {
                    return request.getCall().getChannel();
                }
            });
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
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
