/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.client.GrpcAsyncCall;
import com.quancheng.saluki.core.grpc.router.GrpcRouterFactory;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.core.registry.RegistryProvider;
import com.quancheng.saluki.core.utils.NetUtils;

import io.grpc.Attributes;
import io.grpc.Internal;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;

/**
 * @author shimingliu 2016年12月14日 下午5:15:00
 * @version ThrallNameResolverProvider1.java, v 0.0.1 2016年12月14日 下午5:15:00 shimingliu
 */
@Internal
public class GrpcNameResolverProvider extends NameResolverProvider {

    private static final Logger log = LoggerFactory.getLogger(NameResolverProvider.class);

    private final GrpcURL       subscribeUrl;

    public GrpcNameResolverProvider(GrpcURL refUrl){
        this.subscribeUrl = refUrl;
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
        return new GrpcNameResolver(targetUri, params, subscribeUrl);
    }

    @Override
    public String getDefaultScheme() {
        return null;
    }

    private class GrpcNameResolver extends NameResolver {

        private final Registry                             registry;

        private final GrpcURL                              subscribeUrl;

        @GuardedBy("this")
        private boolean                                    shutdown;

        @GuardedBy("this")
        private Listener                                   listener;

        @GuardedBy("this")
        private volatile List<SocketAddress>               addresses;

        private final NotifyListener.NotifyServiceListener notifyListener = new NotifyListener.NotifyServiceListener() {

                                                                              @Override
                                                                              public void notify(List<GrpcURL> urls) {
                                                                                  if (log.isInfoEnabled()) {
                                                                                      log.info("Grpc nameresolve started listener,Receive notify from registry, prividerUrl is"
                                                                                               + Arrays.toString(urls.toArray()));
                                                                                  }
                                                                                  notifyLoadBalance(urls);
                                                                              }

                                                                          };

        public GrpcNameResolver(URI targetUri, Attributes params, GrpcURL subscribeUrl){
            GrpcURL registryUrl = GrpcURL.valueOf(targetUri.toString());
            this.registry = RegistryProvider.asFactory().newRegistry(registryUrl);
            this.subscribeUrl = subscribeUrl;
        }

        @Override
        public final String getServiceAuthority() {
            return "grpc";
        }

        @Override
        public final synchronized void refresh() {
            Preconditions.checkState(listener != null, "not started");
            List<GrpcURL> urls = registry.discover(subscribeUrl);
            if (log.isInfoEnabled()) {
                log.info("Grpc nameresolve refreshed,Receive notify from registry, prividerUrl is"
                         + Arrays.toString(urls.toArray()));
            }
            notifyLoadBalance(urls);
        }

        @Override
        public final synchronized void start(Listener listener) {
            Preconditions.checkState(this.listener == null, "already started");
            this.listener = listener;
            if (shutdown) {
                return;
            }
            registry.subscribe(subscribeUrl, notifyListener);
            registry.subscribe(subscribeUrl.getGroup(), GrpcRouterFactory.getInstance().getNotifyRouterListener());
        }

        @Override
        public final synchronized void shutdown() {
            if (shutdown) {
                return;
            }
            shutdown = true;
            registry.unsubscribe(subscribeUrl, notifyListener);
        }

        private void notifyLoadBalance(List<GrpcURL> urls) {
            if (urls != null && !urls.isEmpty()) {
                List<ResolvedServerInfo> servers = new ArrayList<ResolvedServerInfo>(urls.size());
                List<SocketAddress> addresses = new ArrayList<SocketAddress>(urls.size());
                for (int i = 0; i < urls.size(); i++) {
                    GrpcURL url = urls.get(i);
                    String host = url.getHost();
                    int port = url.getPort();
                    if (NetUtils.isIP(host)) {
                        SocketAddress sock = new InetSocketAddress(InetAddresses.forString(host), port);
                        addSocketAddress(servers, addresses, sock);
                    } else {
                        try {
                            InetAddress[] inetAddrs = InetAddress.getAllByName(host);
                            for (int j = 0; j < inetAddrs.length; j++) {
                                InetAddress inetAddr = inetAddrs[j];
                                SocketAddress sock = new InetSocketAddress(inetAddr, port);
                                addSocketAddress(servers, addresses, sock);
                            }
                        } catch (UnknownHostException e) {
                            GrpcNameResolver.this.listener.onError(Status.UNAVAILABLE.withCause(e));
                        }
                    }
                }
                this.addresses = addresses;
                Attributes config = this.buildNameResolverConfig();
                GrpcNameResolver.this.listener.onUpdate(Collections.singletonList(servers), config);
            } else {
                GrpcNameResolver.this.listener.onError(Status.NOT_FOUND.withDescription("There is no service registy in consul by"
                                                                                        + subscribeUrl.toFullString()));
            }
        }

        private void addSocketAddress(List<ResolvedServerInfo> servers, List<SocketAddress> addresses,
                                      SocketAddress sock) {
            ResolvedServerInfo serverInfo = new ResolvedServerInfo(sock, Attributes.EMPTY);
            servers.add(serverInfo);
            addresses.add(sock);
        }

        private Attributes buildNameResolverConfig() {
            Attributes.Builder builder = Attributes.newBuilder();
            if (listener != null) {
                builder.set(GrpcAsyncCall.NAMERESOVER_LISTENER, listener);
            }
            if (addresses != null) {
                builder.set(GrpcAsyncCall.REMOTE_ADDR_KEYS_REGISTRY, addresses);
            }
            return builder.build();
        }

    }

}
