package com.quancheng.saluki.serializer.converter;

import com.quancheng.saluki.serializer.IProtobufConverter;
import com.quancheng.saluki.serializer.exception.ProtobufAnnotationException;

public class BooleanConverter implements IProtobufConverter {

    @Override
    public Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        final Boolean b = (Boolean) sourceObject;

        return b;
    }

    @Override
    public Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        final boolean bl = (boolean) sourceObject;

        return new Boolean(bl);
    }
}
