package com.quancheng.saluki.serializer;

import com.google.protobuf.Message;
import com.quancheng.saluki.serializer.exception.ProtobufException;

public interface IProtobufSerializer {

    Message toProtobuf(Object pojo) throws ProtobufException;

    Object fromProtobuf(Message protoBuf, Class<? extends Object> pojoClazz) throws ProtobufException;
}
