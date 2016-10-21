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
