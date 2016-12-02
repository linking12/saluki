package com.quancheng.saluki.core.grpc.utils;

import java.net.SocketAddress;
import java.util.List;

import com.google.common.base.Charsets;
import com.quancheng.saluki.core.common.SalukiURL;

import io.grpc.Attributes;
import io.grpc.Metadata;
import io.grpc.NameResolver;

public final class MarshallersAttributesUtils {

    public static final Attributes.Key<SalukiURL>             PARAMS_DEFAULT_SUBCRIBE   = Attributes.Key.of("subscribe");

    public static final Metadata.Key<String>                  GRPC_CONTEXT_ATTACHMENTS  = Metadata.Key.of("grpc_header_attachments-bin",
                                                                                                          MarshallersAttributesUtils.utf8Marshaller());

    public static final Metadata.Key<String>                  GRPC_CONTEXT_VALUES       = Metadata.Key.of("grpc_header_values-bin",
                                                                                                          MarshallersAttributesUtils.utf8Marshaller());

    public static final Metadata.Key<String>                  GRPC_ERRORCAUSE_VALUE     = Metadata.Key.of("grpc_error_cause-bin",
                                                                                                          MarshallersAttributesUtils.utf8Marshaller());

    public static final Attributes.Key<SocketAddress>         REMOTE_ADDR_KEY           = Attributes.Key.of("remote-addr");

    public static final Attributes.Key<List<SocketAddress>>   REMOTE_ADDR_KEYS          = Attributes.Key.of("remote-addrs");

    public static final Attributes.Key<List<SocketAddress>>   REMOTE_ADDR_KEYS_REGISTRY = Attributes.Key.of("remote-addrs-registry");

    public static final Attributes.Key<NameResolver.Listener> NAMERESOVER_LISTENER      = Attributes.Key.of("nameResolver-Listener");

    private MarshallersAttributesUtils(){
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
