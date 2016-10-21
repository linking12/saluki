package com.quancheng.saluki.core.grpc;

import java.net.URI;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.base.Preconditions;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.cluster.RegistryDirectory;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

public class SalukiNameResolverProvider extends NameResolverProvider {

    private final Attributes attributesParams;

    public SalukiNameResolverProvider(SalukiURL refUrl){
        attributesParams = Attributes.newBuilder().set(SalukiConstants.PARAMS_DEFAULT_SUBCRIBE, refUrl).build();
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
        Attributes allParams = Attributes.newBuilder().setAll(attributesParams).setAll(params).build();
        return new SalukiNameResolver(targetUri, allParams);
    }

    @Override
    public String getDefaultScheme() {
        return null;
    }

    private class SalukiNameResolver extends NameResolver {

        private RegistryDirectory registryDirectory;

        private final SalukiURL   subscribeUrl;

        @GuardedBy("this")
        private boolean           shutdown;

        @GuardedBy("this")
        private Listener          listener;

        public SalukiNameResolver(URI targetUri, Attributes params){
            SalukiURL registryUrl = SalukiURL.valueOf(targetUri.toString());
            this.registryDirectory = new RegistryDirectory.Default();
            registryDirectory.init(registryUrl);
            this.subscribeUrl = params.get(SalukiConstants.PARAMS_DEFAULT_SUBCRIBE);
        }

        @Override
        public final String getServiceAuthority() {
            return "consulauthority";
        }

        @Override
        public final synchronized void refresh() {
            Preconditions.checkState(listener != null, "not started");
            registryDirectory.discover(subscribeUrl);
        }

        @Override
        public final synchronized void start(Listener listener) {
            Preconditions.checkState(this.listener == null, "already started");
            registryDirectory.subscribe(subscribeUrl, listener);
        }

        @Override
        public final synchronized void shutdown() {
            if (shutdown) {
                return;
            }
            shutdown = true;
        }
    }

}
