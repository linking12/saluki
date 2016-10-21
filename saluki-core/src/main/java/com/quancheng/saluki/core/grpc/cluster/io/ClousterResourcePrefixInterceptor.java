package com.quancheng.saluki.core.grpc.cluster.io;

import io.grpc.Metadata;

public class ClousterResourcePrefixInterceptor implements HeaderInterceptor {

    public static final Metadata.Key<String> GRPC_RESOURCE_PREFIX_KEY = Metadata.Key.of("google-cloud-resource-prefix",
                                                                                        Metadata.ASCII_STRING_MARSHALLER);

    private final String                     defaultValue;

    public ClousterResourcePrefixInterceptor(String defaultValue){
        this.defaultValue = defaultValue;
    }

    @Override
    public void updateHeaders(Metadata headers) throws Exception {
        if (!headers.containsKey(GRPC_RESOURCE_PREFIX_KEY)) {
            headers.put(GRPC_RESOURCE_PREFIX_KEY, defaultValue);
        }
    }
}
