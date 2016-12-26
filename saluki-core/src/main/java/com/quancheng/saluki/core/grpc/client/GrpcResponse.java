/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client;

import java.io.Serializable;

import com.google.protobuf.Message;
import com.quancheng.saluki.serializer.exception.ProtobufException;
import com.quancheng.saluki.core.grpc.util.SerializerUtils;

/**
 * @author shimingliu 2016年12月14日 下午5:51:13
 * @version GrpcResponse.java, v 0.0.1 2016年12月14日 下午5:51:13 shimingliu
 */
public interface GrpcResponse {

    public Object getResponseArg() throws ProtobufException;

    public static class Default implements GrpcResponse, Serializable {

        @Override
        public Object getResponseArg() throws ProtobufException {
            return SerializerUtils.Protobuf2Pojo(this.getMessage(), this.getReturnType());
        }

        private static final long serialVersionUID = 1L;

        private final Message     message;

        private final Class<?>    returnType;

        public Default(Message message, Class<?> returnType){
            super();
            this.message = message;
            this.returnType = returnType;
        }

        public Message getMessage() {
            return message;
        }

        public Class<?> getReturnType() {
            return returnType;
        }

    }
}
