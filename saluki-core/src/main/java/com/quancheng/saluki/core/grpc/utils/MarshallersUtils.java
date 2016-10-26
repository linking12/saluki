package com.quancheng.saluki.core.grpc.utils;

import com.google.common.base.Charsets;
import com.quancheng.saluki.core.common.SalukiURL;

import io.grpc.Attributes;
import io.grpc.Metadata;

import java.util.Objects;

public final class MarshallersUtils {

    public static final Attributes.Key<SalukiURL> PARAMS_DEFAULT_SUBCRIBE  = Attributes.Key.of("subscribe");

    public static Metadata.Key<byte[]>            GRPC_CONTEXT_ATTACHMENTS = Metadata.Key.of("grpc_header_attachments-bin",
                                                                                             Metadata.BINARY_BYTE_MARSHALLER);

    public static Metadata.Key<byte[]>            GRPC_CONTEXT_VALUES      = Metadata.Key.of("grpc_header_values-bin",
                                                                                             Metadata.BINARY_BYTE_MARSHALLER);

    public static Metadata.Key<String>            GRPC_ERRORCAUSE_VALUE     = Metadata.Key.of("grpc_error_cause-bin",
                                                                                             MarshallersUtils.utf8Marshaller());

    private MarshallersUtils(){
    }

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
