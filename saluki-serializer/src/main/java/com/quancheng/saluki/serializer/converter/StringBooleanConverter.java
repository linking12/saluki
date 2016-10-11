package com.quancheng.saluki.serializer.converter;

import com.quancheng.saluki.serializer.IProtobufConverter;
import com.quancheng.saluki.serializer.exception.ProtobufAnnotationException;

public class StringBooleanConverter implements IProtobufConverter {

    @Override
    public Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        final String bl = (String) sourceObject;
        return new Boolean(bl).booleanValue();
    }

    @Override
    public Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        final boolean b = (boolean) sourceObject;
        return new Boolean(b).toString();
    }
}
