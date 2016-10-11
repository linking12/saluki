
package com.quancheng.saluki.serializer.converter;

import java.math.BigDecimal;

import com.quancheng.saluki.serializer.IProtobufConverter;
import com.quancheng.saluki.serializer.exception.ProtobufAnnotationException;

public class BigDecimalStringConverter implements IProtobufConverter {

    @Override
    public Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        final BigDecimal bd = (BigDecimal) sourceObject;

        return bd.toPlainString();
    }

    @Override
    public Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        return new BigDecimal((String) sourceObject);
    }
}
