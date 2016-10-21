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
package com.quancheng.saluki.core.grpc.cluster.async;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientCall.Listener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * Utilities for creating and executing async methods.
 *
 * @author sduskis
 * @version $Id: $Id
 */
public interface SalukiAsyncUtilities {

    <RequestT, ResponseT> SalukiAsyncRpc<RequestT, ResponseT> createAsyncRpc(MethodDescriptor<RequestT, ResponseT> method,
                                                                             Predicate<RequestT> isRetryable);

    public static class Default implements SalukiAsyncUtilities {

        private final Channel channel;

        public Default(Channel channel){
            this.channel = channel;
        }

        @Override
        public <RequestT, ResponseT> SalukiAsyncRpc<RequestT, ResponseT> createAsyncRpc(final MethodDescriptor<RequestT, ResponseT> method,
                                                                                        final Predicate<RequestT> isRetryable) {
            return new SalukiAsyncRpc<RequestT, ResponseT>() {

                @Override
                public boolean isRetryable(RequestT request) {
                    return isRetryable.apply(request);
                }

                @Override
                public MethodDescriptor<RequestT, ResponseT> getMethodDescriptor() {
                    return method;
                }

                @Override
                public ClientCall<RequestT, ResponseT> newCall(CallOptions callOptions) {
                    return channel.newCall(method, callOptions);
                }

                @Override
                public void start(ClientCall<RequestT, ResponseT> call, RequestT request, Listener<ResponseT> listener,
                                  Metadata metadata) {
                    call.start(listener, metadata);
                    call.request(1);
                    try {
                        call.sendMessage(request);
                    } catch (Throwable t) {
                        call.cancel("Exception in sendMessage.", t);
                        throw Throwables.propagate(t);
                    }
                    try {
                        call.halfClose();
                    } catch (Throwable t) {
                        call.cancel("Exception in halfClose.", t);
                        throw Throwables.propagate(t);
                    }
                }
            };
        }
    }
}
