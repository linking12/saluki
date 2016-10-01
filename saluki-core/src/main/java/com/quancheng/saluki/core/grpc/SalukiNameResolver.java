package com.quancheng.saluki.core.grpc;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.core.registry.RegistryProvider;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;

public class SalukiNameResolver extends NameResolver {

    private final String    authority;

    private final Registry  registry;

    private final SalukiURL subscribeUrl;

    @GuardedBy("this")
    private boolean         shutdown;

    @GuardedBy("this")
    private Listener        listener;

    public SalukiNameResolver(@Nullable String nsAuthority, String name, Attributes params){
        URI nameUri = URI.create("//" + name);
        authority = Preconditions.checkNotNull(nameUri.getAuthority(), "nameUri (%s) doesn't have an authority",
                                               nameUri);
        final String host = Preconditions.checkNotNull(nameUri.getHost(), "host");
        final int port;
        if (nameUri.getPort() == -1) {
            Integer defaultPort = params.get(NameResolver.Factory.PARAMS_DEFAULT_PORT);
            if (defaultPort != null) {
                port = defaultPort;
            } else {
                throw new IllegalArgumentException("name '" + name
                                                   + "' doesn't contain a port, and default port is not set in params");
            }
        } else {
            port = nameUri.getPort();
        }
        SalukiURL registryUrl = new SalukiURL(SalukiConstants.REGISTRY_PROTOCOL, host, port);
        registry = RegistryProvider.asFactory().newRegistry(registryUrl);
        subscribeUrl = params.get(SalukiConstants.PARAMS_DEFAULT_SUBCRIBE);
    }

    @Override
    public final String getServiceAuthority() {
        return authority;
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
            List<ResolvedServerInfo> servers = new ArrayList<ResolvedServerInfo>(urls.size());
            for (int i = 0; i < urls.size(); i++) {
                SalukiURL url = urls.get(i);
                String ip = url.getHost();
                int port = url.getPort();
                servers.add(new ResolvedServerInfo(new InetSocketAddress(InetAddresses.forString(ip), port),
                                                   Attributes.EMPTY));
            }
            SalukiNameResolver.this.listener.onUpdate(Collections.singletonList(servers), Attributes.EMPTY);
        } else {
            SalukiNameResolver.this.listener.onError(Status.NOT_FOUND.withDescription("There is no service registy in consul by"
                                                                                      + subscribeUrl.toFullString()));
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
