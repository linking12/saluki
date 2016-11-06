
package com.quancheng.saluki.serializer.converter;

import java.math.BigDecimal;

import com.quancheng.saluki.serializer.IProtobufConverter;
import com.quancheng.saluki.serializer.exception.ProtobufAnnotationException;

public class BigDecimalDoubleConverter implements IProtobufConverter {

    @Override
    public Object convertToProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        final BigDecimal bd = (BigDecimal) sourceObject;

        return bd.doubleValue();
    }

    @Override
    public Object convertFromProtobuf(Object sourceObject) throws ProtobufAnnotationException {
        final double bd = (double) sourceObject;

        return new BigDecimal(bd);
    }
}
