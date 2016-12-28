package com.quancheng.saluki.boot.jaket.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.quancheng.saluki.boot.jaket.JaketTypeBuilder;
import com.quancheng.saluki.boot.jaket.model.TypeDefinition;
import com.quancheng.saluki.boot.jaket.util.ClassUtils;
import com.quancheng.saluki.boot.jaket.util.JaketConfigurationUtils;

/**
 * Created by huangsheng.hs on 2015/1/27.
 */
public final class DefaultTypeBuilder {

    public static TypeDefinition build(Class<?> clazz, Map<Class<?>, TypeDefinition> typeCache) {
        final String canonicalName = clazz.getCanonicalName();

        TypeDefinition td = new TypeDefinition(canonicalName);
        // Try to get a cached definition
        if (typeCache.containsKey(clazz)) {
            return typeCache.get(clazz);
        }

        // Primitive type
        if (!JaketConfigurationUtils.needAnalyzing(clazz)) {
            return td;
        }

        // Custom type
        TypeDefinition ref = new TypeDefinition(canonicalName);
        ref.set$ref(canonicalName);
        typeCache.put(clazz, ref);

        List<Field> fields = ClassUtils.getNonStaticFields(clazz);
        for (Field field : fields) {
            String name = field.getName();
            Class<?> fieldClass = field.getType();
            Type fieldType = field.getGenericType();

            TypeDefinition fieldTd = JaketTypeBuilder.build(fieldType,fieldClass, typeCache);
            td.getProperties().put(name, fieldTd);
        }

        typeCache.put(clazz, td);
        return td;
    }

    private DefaultTypeBuilder() {
    }
}
