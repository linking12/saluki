package com.quancheng.saluki.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.protobuf.GeneratedMessageV3;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProtobufEntity {

    Class<? extends GeneratedMessageV3> value();
}
