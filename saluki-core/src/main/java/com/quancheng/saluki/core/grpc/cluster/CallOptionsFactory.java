/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.saluki.core.grpc.cluster;

import java.util.concurrent.TimeUnit;

import com.quancheng.saluki.core.grpc.cluster.config.CallOptionsConfig;

import io.grpc.CallOptions;
import io.grpc.Deadline;
import io.grpc.MethodDescriptor;

/**
 * A factory that creates {@link io.grpc.CallOptions} for use in
 * {@link com.google.cloud.bigtable.grpc.BigtableDataClient} RPCs.
 *
 * @author sduskis
 * @version $Id: $Id
 */
public interface CallOptionsFactory {

    /**
     * Provide a {@link io.grpc.CallOptions} object to be used in a single RPC. {@link io.grpc.CallOptions} can contain
     * state, specifically start time with an expiration is set; in cases when timeouts are used, implementations should
     * create a new CallOptions each time this method is called.
     *
     * @param descriptor The RPC that's being called. Different methods have different performance characteristics, so
     * this parameter can be useful to craft the right timeout for the right method.
     * @param request Some methods, specifically ReadRows, can have variability depending on the request. The request
     * can be for either a single row, or a range. This parameter can be used to tune timeouts
     * @param <RequestT> a RequestT object.
     * @return a {@link io.grpc.CallOptions} object.
     */
    <RequestT> CallOptions create(MethodDescriptor<RequestT, ?> descriptor, RequestT request);

    /**
     * Always returns {@link CallOptions#DEFAULT}.
     */
    public static class Default implements CallOptionsFactory {

        @Override
        public <RequestT> CallOptions create(MethodDescriptor<RequestT, ?> descriptor, RequestT request) {
            return CallOptions.DEFAULT;
        }
    }

    /** Creates a new {@link CallOptions} based on a {@link CallOptionsConfig}. */
    public static class ConfiguredCallOptionsFactory implements CallOptionsFactory {

        private final CallOptionsConfig config;

        public ConfiguredCallOptionsFactory(CallOptionsConfig config){
            this.config = config;
        }

        @Override
        public <RequestT> CallOptions create(MethodDescriptor<RequestT, ?> descriptor, RequestT request) {
            if (!config.isUseTimeout() || request == null) {
                return CallOptions.DEFAULT;
            }

            int timeout = config.getShortRpcTimeoutMs();
            return CallOptions.DEFAULT.withDeadline(Deadline.after(timeout, TimeUnit.MILLISECONDS));
        }
    }
}
