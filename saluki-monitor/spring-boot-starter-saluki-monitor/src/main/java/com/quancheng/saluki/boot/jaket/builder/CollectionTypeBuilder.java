package com.quancheng.saluki.boot.jaket.builder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

import com.quancheng.saluki.boot.jaket.JaketTypeBuilder;
import com.quancheng.saluki.boot.jaket.model.TypeDefinition;

/**
 * Created by huangsheng.hs on 2015/1/27.
 */
public class CollectionTypeBuilder implements TypeBuilder {

    @Override
    public boolean accept(Type type, Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            return true;
        }

        return false;
    }

    @Override
    public TypeDefinition build(Type type, Class<?> clazz, Map<Class<?>, TypeDefinition> typeCache) {
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException(MessageFormat.format("[Jaket] Unexpected type {0}.",
                    new Object[]{type}));
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] actualTypeArgs = parameterizedType.getActualTypeArguments();
        if (actualTypeArgs == null || actualTypeArgs.length != 1) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "[Jaket] Collection type [{0}] with unexpected amount of arguments [{1}]." + actualTypeArgs,
                    new Object[] { type, actualTypeArgs }));
        }

        Type actualType = actualTypeArgs[0];
        if (actualType instanceof ParameterizedType) {
            // Nested collection or map.
            Class<?> rawType = (Class<?>) ((ParameterizedType) actualType).getRawType();
            JaketTypeBuilder.build(actualType, rawType, typeCache);
        } else if (actualType instanceof Class<?>) {
            Class<?> actualClass = (Class<?>) actualType;
            if (actualClass.isArray() || actualClass.isEnum()) {
                JaketTypeBuilder.build(null, actualClass, typeCache);
            } else {
                DefaultTypeBuilder.build(actualClass, typeCache);
            }
        }

        TypeDefinition td = new TypeDefinition(type.toString());
        return td;
    }

}
