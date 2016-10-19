package com.quancheng.saluki.core.grpc.filter;

import java.io.Serializable;

import com.google.protobuf.Message;

public class GrpcResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Message           message;

    private Class<?>          returnType;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

}
