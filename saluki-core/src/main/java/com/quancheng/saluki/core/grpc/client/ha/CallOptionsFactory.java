package com.quancheng.saluki.core.grpc.client.ha;

import java.net.SocketAddress;
import java.util.List;

import io.grpc.Attributes;
import io.grpc.CallOptions;
import io.grpc.MethodDescriptor;
import io.grpc.NameResolver;

public interface CallOptionsFactory {

    public static final Attributes.Key<SocketAddress>         REMOTE_ADDR_KEY      = Attributes.Key.of("remote-addr");
    public static final Attributes.Key<List<SocketAddress>>   REMOTE_ADDR_KEYS     = Attributes.Key.of("remote-addrs");
    public static final Attributes.Key<NameResolver.Listener> NAMERESOVER_LISTENER = Attributes.Key.of("nameResolver-Listener");

    <RequestT> CallOptions create(MethodDescriptor<RequestT, ?> descriptor, RequestT request);

    public static class Default implements CallOptionsFactory {

        @Override
        public <RequestT> CallOptions create(MethodDescriptor<RequestT, ?> descriptor, RequestT request) {
            return CallOptions.DEFAULT.withAffinity(Attributes.EMPTY);
        }
    }

}
