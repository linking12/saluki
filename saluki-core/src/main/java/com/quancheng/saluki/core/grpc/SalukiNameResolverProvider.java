package com.quancheng.saluki.core.grpc;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.client.ha.CallOptionsFactory;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.core.registry.RegistryProvider;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;

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

        private final Registry  registry;

        private final SalukiURL subscribeUrl;

        @GuardedBy("this")
        private boolean         shutdown;

        @GuardedBy("this")
        private Listener        listener;

        public SalukiNameResolver(URI targetUri, Attributes params){
            SalukiURL registryUrl = SalukiURL.valueOf(targetUri.toString());
            registry = RegistryProvider.asFactory().newRegistry(registryUrl);
            subscribeUrl = params.get(SalukiConstants.PARAMS_DEFAULT_SUBCRIBE);
        }

        @Override
        public final String getServiceAuthority() {
            return "consulauthority";
        }

        @Override
        public final synchronized void refresh() {
            Preconditions.checkState(listener != null, "not started");
            List<SalukiURL> urls = registry.discover(subscribeUrl);
            notifyLoadBalance(urls);
        }

        private NotifyListener notifyListener = new NotifyListener() {

            @Override
            public void notify(List<SalukiURL> urls) {
                notifyLoadBalance(urls);
            }

        };

        private void notifyLoadBalance(List<SalukiURL> urls) {
            Attributes config = this.buildNameResolverConfig();
            if (urls != null && !urls.isEmpty()) {
                List<ResolvedServerInfo> servers = new ArrayList<ResolvedServerInfo>(urls.size());
                for (int i = 0; i < urls.size(); i++) {
                    SalukiURL url = urls.get(i);
                    String ip = url.getHost();
                    int port = url.getPort();
                    SocketAddress sock = new InetSocketAddress(InetAddresses.forString(ip), port);
                    ResolvedServerInfo serverInfo = new ResolvedServerInfo(sock, config);
                    servers.add(serverInfo);
                }
                SalukiNameResolver.this.listener.onUpdate(Collections.singletonList(servers), config);
            } else {
                SalukiNameResolver.this.listener.onError(Status.NOT_FOUND.withDescription("There is no service registy in consul by"
                                                                                          + subscribeUrl.toFullString()));
            }
        }

        private Attributes buildNameResolverConfig() {
            if (listener != null) {
                return Attributes.newBuilder().set(CallOptionsFactory.NAMERESOVER_LISTENER, listener).build();
            } else {
                return Attributes.EMPTY;
            }
        }

        @Override
        public final synchronized void start(Listener listener) {
            Preconditions.checkState(this.listener == null, "already started");
            this.listener = listener;
            resolve();
        }

        private void resolve() {
            if (shutdown) {
                return;
            }
            registry.subscribe(subscribeUrl, notifyListener);
        }

        @Override
        public final synchronized void shutdown() {
            if (shutdown) {
                return;
            }
            shutdown = true;
            registry.unsubscribe(subscribeUrl, notifyListener);
        }
    }

}
