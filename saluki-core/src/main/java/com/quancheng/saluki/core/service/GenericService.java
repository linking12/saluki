package com.quancheng.saluki.core.service;

public interface GenericService {

    Object $invoke(String serviceName, String method, String[] parameterTypes, Object[] args) throws GenericException;
}
