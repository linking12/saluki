package com.quancheng.saluki.core.grpc.filter;

import java.io.Serializable;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;

public interface GrpcResponse {

    public Object getResponseArg();

    public static class Default implements GrpcResponse, Serializable {

        @Override
        public Object getResponseArg() {
            return PojoProtobufUtils.Protobuf2Pojo(this.getMessage(), this.getReturnType());
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
