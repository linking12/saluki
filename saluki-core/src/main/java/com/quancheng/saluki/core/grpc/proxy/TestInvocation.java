package com.quancheng.saluki.core.grpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TestInvocation implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return "hello";
    }

}
