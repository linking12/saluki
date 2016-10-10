package com.quancheng.saluki.core.grpc.service;

public interface GenericService {

    Object $invoke(String serviceName, String method, String[] parameterTypes, Object[] args) throws GenericException;
}
