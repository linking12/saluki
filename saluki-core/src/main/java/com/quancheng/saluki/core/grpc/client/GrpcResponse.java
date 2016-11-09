package com.quancheng.saluki.core.grpc.client;

import java.io.Serializable;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;
import com.quancheng.saluki.serializer.exception.ProtobufException;

public interface GrpcResponse {

    public Object getResponseArg() throws ProtobufException;

    public static class Default implements GrpcResponse, Serializable {

        @Override
        public Object getResponseArg() throws ProtobufException {
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
