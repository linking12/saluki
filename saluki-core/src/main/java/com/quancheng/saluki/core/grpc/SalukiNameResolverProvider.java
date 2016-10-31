package com.quancheng.saluki.core.grpc;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.client.ha.internal.CallOptionsFactory;
import com.quancheng.saluki.core.grpc.utils.MarshallersUtils;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.core.registry.RegistryProvider;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;

public class SalukiNameResolverProvider extends NameResolverProvider {

    private static final Logger log = LoggerFactory.getLogger(NameResolverProvider.class);

    private final Attributes    attributesParams;

    public SalukiNameResolverProvider(SalukiURL refUrl){
        attributesParams = Attributes.newBuilder().set(MarshallersUtils.PARAMS_DEFAULT_SUBCRIBE, refUrl).build();
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

        private final Registry               registry;

        private final SalukiURL              subscribeUrl;

        @GuardedBy("this")
        private boolean                      shutdown;

        @GuardedBy("this")
        private Listener                     listener;

        @GuardedBy("this")
        private volatile List<SocketAddress> addresses;

        public SalukiNameResolver(URI targetUri, Attributes params){
            SalukiURL registryUrl = SalukiURL.valueOf(targetUri.toString());
            registry = RegistryProvider.asFactory().newRegistry(registryUrl);
            subscribeUrl = params.get(MarshallersUtils.PARAMS_DEFAULT_SUBCRIBE);
        }

        @Override
        public final String getServiceAuthority() {
            return "grpc";
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
            if (urls != null && !urls.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.info("Receive notify from registry, subscribeUrl is " + subscribeUrl + " prividerUrl is"
                             + Arrays.toString(urls.toArray()));
                }
                List<ResolvedServerInfo> servers = new ArrayList<ResolvedServerInfo>(urls.size());
                List<SocketAddress> addresses = new ArrayList<SocketAddress>(urls.size());
                for (int i = 0; i < urls.size(); i++) {
                    SalukiURL url = urls.get(i);
                    String ip = url.getHost();
                    int port = url.getPort();
                    SocketAddress sock = new InetSocketAddress(InetAddresses.forString(ip), port);
                    ResolvedServerInfo serverInfo = new ResolvedServerInfo(sock, Attributes.EMPTY);
                    servers.add(serverInfo);
                    addresses.add(sock);
                }
                this.addresses = addresses;
                Attributes config = this.buildNameResolverConfig();
                SalukiNameResolver.this.listener.onUpdate(Collections.singletonList(servers), config);
            } else {
                SalukiNameResolver.this.listener.onError(Status.NOT_FOUND.withDescription("There is no service registy in consul by"
                                                                                          + subscribeUrl.toFullString()));
            }
        }

        private Attributes buildNameResolverConfig() {
            Attributes.Builder builder = Attributes.newBuilder();
            if (listener != null) {
                builder.set(CallOptionsFactory.NAMERESOVER_LISTENER, listener);
            }
            if (addresses != null) {
                builder.set(CallOptionsFactory.REMOTE_ADDR_KEYS_REGISTRY, addresses);
            }
            return builder.build();
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
