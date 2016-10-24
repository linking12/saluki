package com.quancheng.saluki.core.grpc.utils;

import com.google.common.base.Charsets;
import io.grpc.Metadata;

import java.util.Objects;

public final class Marshallers {
    private Marshallers() {}

    public static <E extends Enum<E>> Metadata.BinaryMarshaller<E> enumMarshaller(Class<E> enumType, E defaultValue) {
        Objects.requireNonNull(enumType, "enumType");
        return new Metadata.BinaryMarshaller<E>() {
            @Override
            public byte[] toBytes(E value) {
                return value.name().getBytes(Charsets.UTF_8);
            }

            @Override
            public E parseBytes(byte[] serialized) {
                String name = new String(serialized, Charsets.UTF_8);
                try {
                    return Enum.valueOf(enumType, name);
                } catch (IllegalArgumentException e) {
                    if (defaultValue != null) {
                        return defaultValue;
                    }
                    throw e;
                }
            }
        };
    }

    public static Metadata.BinaryMarshaller<String> utf8Marshaller() {
        return new Metadata.BinaryMarshaller<String>() {
            @Override
            public byte[] toBytes(String value) {
                return value.getBytes(Charsets.UTF_8);
            }

            @Override
            public String parseBytes(byte[] serialized) {
                return new String(serialized, Charsets.UTF_8);
            }
        };
    }
}
