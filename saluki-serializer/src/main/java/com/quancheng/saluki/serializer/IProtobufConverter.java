package com.quancheng.saluki.serializer;

import com.quancheng.saluki.serializer.exception.ProtobufAnnotationException;

public interface IProtobufConverter {

    Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException;

    Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException;
}
