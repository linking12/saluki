package com.quancheng.saluki.core.grpc.client;

import com.quancheng.saluki.core.grpc.filter.GrpcResponse;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;

public final class SalukiResponse {

    private final GrpcResponse response;

    public SalukiResponse(GrpcResponse response){
        this.response = response;
    }

    public Object getResponseArg() {
        return PojoProtobufUtils.Protobuf2Pojo(response.getMessage(), response.getReturnType());
    }

}
