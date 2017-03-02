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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.client.GrpcAsyncCall;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.NotifyListener.NotifyRouterListener;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.core.registry.RegistryProvider;
import com.quancheng.saluki.core.utils.NetUtils;

import io.grpc.Attributes;
import io.grpc.Internal;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.ResolvedServerInfo;
import io.grpc.ResolvedServerInfoGroup;
import io.grpc.Status;

/**
 * @author shimingliu 2016年12月14日 下午5:15:00
 * @version ThrallNameResolverProvider1.java, v 0.0.1 2016年12月14日 下午5:15:00 shimingliu
 */
@Internal
public class GrpcNameResolverProvider extends NameResolverProvider {

    private static final Logger                                           log                          = LoggerFactory.getLogger(NameResolverProvider.class);

    public static final Attributes.Key<String>                            GRPC_ROUTER_MESSAGE          = Attributes.Key.of("grpc-router");

    public static final Attributes.Key<Map<List<SocketAddress>, GrpcURL>> GRPC_ADDRESS_GRPCURL_MAPPING = Attributes.Key.of("grpc-address-mapping");

    private final GrpcURL                                                 subscribeUrl;

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

        @GuardedBy("this")
        private volatile List<GrpcURL>                     urls;

        private final Map<String, String>                  routerMessages = Maps.newConcurrentMap();

        private final NotifyListener.NotifyServiceListener notifyListener = new NotifyListener.NotifyServiceListener() {

                                                                              @Override
                                                                              public void notify(List<GrpcURL> urls) {
                                                                                  if (log.isInfoEnabled()) {
                                                                                      log.info("Grpc nameresolve started listener,Receive notify from registry, prividerUrl is"
                                                                                               + Arrays.toString(urls.toArray()));
                                                                                  }
                                                                                  GrpcNameResolver.this.urls = urls;
                                                                                  notifyLoadBalance(urls);
                                                                              }

                                                                          };
        private final NotifyListener.NotifyRouterListener  routerListener = new NotifyRouterListener() {

                                                                              @Override
                                                                              public void notify(String group,
                                                                                                 String routerCondition) {
                                                                                  if (routerCondition == null) {
                                                                                      routerMessages.remove(group);
                                                                                  } else {
                                                                                      routerMessages.put(group,
                                                                                                         routerCondition);
                                                                                  }
                                                                                  if (GrpcNameResolver.this.urls != null) {
                                                                                      notifyLoadBalance(GrpcNameResolver.this.urls);
                                                                                  }
                                                                              }

                                                                          };

        public GrpcNameResolver(URI targetUri, Attributes params, GrpcURL subscribeUrl){
            GrpcURL registryUrl = GrpcURL.valueOf(targetUri.toString());
            this.registry = RegistryProvider.asFactory().newRegistry(registryUrl);
            this.subscribeUrl = subscribeUrl;
            registry.subscribe(subscribeUrl.getGroup(), routerListener);
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
                List<ResolvedServerInfo> servers = Lists.newArrayList();
                List<SocketAddress> addresses = Lists.newArrayList();
                Map<List<SocketAddress>, GrpcURL> addressUrlMapping = Maps.newHashMap();
                for (GrpcURL url : urls) {
                    String host = url.getHost();
                    int port = url.getPort();
                    List<SocketAddress> hostAddressMapping;
                    if (NetUtils.isIP(host)) {
                        hostAddressMapping = IpResolved(servers, addresses, host, port);
                    } else {
                        hostAddressMapping = DnsResolved(servers, addresses, host, port);
                    }
                    addressUrlMapping.put(hostAddressMapping, url);
                }
                this.addresses = addresses;
                Attributes config = this.buildAttributes(addressUrlMapping);
                ResolvedServerInfoGroup serversGroup = ResolvedServerInfoGroup.builder().addAll(servers).build();
                GrpcNameResolver.this.listener.onUpdate(Collections.singletonList(serversGroup), config);
            } else {
                GrpcNameResolver.this.listener.onError(Status.NOT_FOUND.withDescription("There is no service registy in consul by"
                                                                                        + subscribeUrl.toFullString()));
            }
        }

        private List<SocketAddress> DnsResolved(List<ResolvedServerInfo> servers, List<SocketAddress> addresses,
                                                String host, int port) {
            List<SocketAddress> hostAddressMapping = Lists.newArrayList();
            try {
                InetAddress[] inetAddrs = InetAddress.getAllByName(host);
                for (int j = 0; j < inetAddrs.length; j++) {
                    InetAddress inetAddr = inetAddrs[j];
                    SocketAddress sock = new InetSocketAddress(inetAddr, port);
                    hostAddressMapping.add(sock);
                    addSocketAddress(servers, addresses, sock);
                }
                return hostAddressMapping;
            } catch (UnknownHostException e) {
                GrpcNameResolver.this.listener.onError(Status.UNAVAILABLE.withCause(e));
            }
            return hostAddressMapping;
        }

        private List<SocketAddress> IpResolved(List<ResolvedServerInfo> servers, List<SocketAddress> addresses,
                                               String host, int port) {
            List<SocketAddress> hostAddressMapping = Lists.newArrayList();
            SocketAddress sock = new InetSocketAddress(InetAddresses.forString(host), port);
            hostAddressMapping.add(sock);
            addSocketAddress(servers, addresses, sock);
            return hostAddressMapping;
        }

        private void addSocketAddress(List<ResolvedServerInfo> servers, List<SocketAddress> addresses,
                                      SocketAddress sock) {
            ResolvedServerInfo serverInfo = new ResolvedServerInfo(sock, Attributes.EMPTY);
            servers.add(serverInfo);
            addresses.add(sock);
        }

        private Attributes buildAttributes(Map<List<SocketAddress>, GrpcURL> addressUrlMapping) {
            Attributes.Builder builder = Attributes.newBuilder();
            if (listener != null) {
                builder.set(GrpcAsyncCall.NAMERESOVER_LISTENER, listener);
            }
            if (addresses != null) {
                builder.set(GrpcAsyncCall.REGISTRY_REMOTE_ADDR_KEYS, addresses);
            }
            String routeMessage = this.routerMessages.get(subscribeUrl.getGroup());
            if (routeMessage != null) {
                builder.set(GRPC_ROUTER_MESSAGE, routeMessage);
            }
            if (!addressUrlMapping.isEmpty()) {
                builder.set(GRPC_ADDRESS_GRPCURL_MAPPING, addressUrlMapping);
            }
            return builder.build();
        }

    }

}
