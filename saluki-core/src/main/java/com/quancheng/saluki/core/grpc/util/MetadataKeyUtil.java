/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.util;

import com.google.common.base.Charsets;

import io.grpc.Metadata;

/**
 * @author shimingliu 2016年12月15日 下午9:49:32
 * @version MetadataKeyUtil.java, v 0.0.1 2016年12月15日 下午9:49:32 shimingliu
 */
public final class MetadataKeyUtil {

    public static final Metadata.Key<String> GRPC_CONTEXT_ATTACHMENTS = Metadata.Key.of("grpc_header_attachments-bin",
                                                                                        utf8Marshaller());

    public static final Metadata.Key<String> GRPC_CONTEXT_VALUES      = Metadata.Key.of("grpc_header_values-bin",
                                                                                        utf8Marshaller());

    public static final Metadata.Key<String> GRPC_ERRORCAUSE_VALUE    = Metadata.Key.of("grpc_error_cause-bin",
                                                                                        utf8Marshaller());

    private static Metadata.BinaryMarshaller<String> utf8Marshaller() {
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

    private MetadataKeyUtil(){

    }
}
