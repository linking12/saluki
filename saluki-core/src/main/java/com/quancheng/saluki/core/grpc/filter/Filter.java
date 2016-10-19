package com.quancheng.saluki.core.grpc.filter;

public interface Filter {

    public void before(GrpcRequest request);

    public void after(GrpcResponse response);

}
