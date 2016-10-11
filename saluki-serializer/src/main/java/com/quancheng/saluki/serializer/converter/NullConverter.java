package com.quancheng.saluki.serializer.converter;

import com.quancheng.saluki.serializer.IProtobufConverter;
import com.quancheng.saluki.serializer.exception.ProtobufAnnotationException;

public class NullConverter implements IProtobufConverter {

    @Override
    public Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        return sourceObject;
    }

    @Override
    public Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        return sourceObject;
    }
}
