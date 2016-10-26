package com.quancheng.examples.service;

/**
 * The greeting service definition.
 */
public interface HelloService { 
    /**
     * Sends a greeting
     */
    public com.quancheng.examples.model.hello.HelloReply sayHello(com.quancheng.examples.model.hello.HelloRequest request);
} 
