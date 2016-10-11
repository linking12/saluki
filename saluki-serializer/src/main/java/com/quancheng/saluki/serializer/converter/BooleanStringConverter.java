package com.quancheng.saluki.serializer.converter;

import com.quancheng.saluki.serializer.IProtobufConverter;
import com.quancheng.saluki.serializer.exception.ProtobufAnnotationException;

public class BooleanStringConverter implements IProtobufConverter {

    @Override
    public Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        final Boolean b = (Boolean) sourceObject;
        return b.toString();
    }

    @Override
    public Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        final String bl = (String) sourceObject;

        return new Boolean(bl);
    }
}
