package com.quancheng.saluki.core.grpc.proxy;

import java.lang.reflect.Proxy;

import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.core.utils.ClassHelper;

public class TestMain {

    public static void main(String[] args) {

        Object obj = Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { GenericService.class },
                                            new TestInvocation());
    }

}
