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

public interface CallOptionsFactory {

    <RequestT> CallOptions create(MethodDescriptor<RequestT, ?> descriptor, RequestT request);

    public static class Default implements CallOptionsFactory {

        @Override
        public <RequestT> CallOptions create(MethodDescriptor<RequestT, ?> descriptor, RequestT request) {
            return CallOptions.DEFAULT;
        }
    }

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
