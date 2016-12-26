package com.quancheng.saluki.boot.jaket.builder;


import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import com.quancheng.saluki.boot.jaket.model.TypeDefinition;


/**
 * Created by huangsheng.hs on 2015/1/27.
 */
public class EnumTypeBuilder implements TypeBuilder {

    @Override
    public boolean accept(Type type, Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        if (clazz.isEnum()) {
            return true;
        }

        return false;
    }

    @Override
    public TypeDefinition build(Type type, Class<?> clazz, Map<Class<?>, TypeDefinition> typeCache) {
        TypeDefinition td = new TypeDefinition(clazz.getCanonicalName());

        try {
            Method methodValues = clazz.getDeclaredMethod("values", new Class<?>[0]);
            Object[] values = (Object[]) methodValues.invoke(clazz, new Object[0]);
            int length = values.length;
            for (int i = 0; i < length; i++) {
                Object value = values[i];
                td.getEnums().add(value.toString());
            }
        } catch (Throwable t) {
            td.setId("-1");
        }

        typeCache.put(clazz, td);
        return td;
    }

}
