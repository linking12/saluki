package com.quancheng.saluki.serializer.pojo;

import java.lang.annotation.Annotation;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.quancheng.saluki.serializer.ProtobufEntity;

public class TestMain {

    public static Annotation findAnnotation(Class<?> target, Class<? extends Annotation> annotation) {
        for (Annotation targetAnnotation : target.getAnnotations()) {
            if (annotation.isAssignableFrom(targetAnnotation.annotationType())) {
                return targetAnnotation;
            } else {
                continue;
            }
        }
        return null;
    }

    public static void main(String[] args) {

        ProtobufEntity a = (ProtobufEntity) findAnnotation(Address.class, ProtobufEntity.class);
        System.out.println(a.value());
    }

}
