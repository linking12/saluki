package com.quancheng.saluki.core.grpc.cluster;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.api.client.util.Lists;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.InetAddresses;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.core.registry.RegistryProvider;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;

public interface RegistryDirectory {

    public void subscribe(SalukiURL subscribeUrl, NameResolver.Listener listener);

    public void discover(SalukiURL subscribeUrl);

    public void remove(SalukiURL subscribeUrl, ResolvedServerInfo serverInfo);

    public void init(SalukiURL registryUrl);

    public static RegistryDirectory getInstance() {
        if (RegistryDirectory.Default.directory == null) {
            throw new IllegalArgumentException("Directory is not init,which is init in SalukiNameResolverProvider SalukiNameResolver");
        }
        return RegistryDirectory.Default.directory;
    }

    public static class Default implements RegistryDirectory {

        private static RegistryDirectory                   directory;

        private Cache<SalukiURL, List<ResolvedServerInfo>> resolvedServerInfos;

        private Cache<SalukiURL, NameResolver.Listener>    resolvedListeners;

        private Registry                                   registry;

        public void init(SalukiURL registryUrl) {
            this.registry = RegistryProvider.asFactory().newRegistry(registryUrl);
            this.resolvedServerInfos = CacheBuilder.newBuilder()//
                                                   .softValues()//
                                                   .ticker(Ticker.systemTicker())//
                                                   .build();
            this.resolvedListeners = CacheBuilder.newBuilder()//
                                                 .softValues()//
                                                 .ticker(Ticker.systemTicker())//
                                                 .build();
            directory = this;
        }

        public void subscribe(SalukiURL subscribeUrl, NameResolver.Listener listener) {
            resolvedListeners.put(subscribeUrl, listener);
            registry.subscribe(subscribeUrl, new NotifyListener() {

                @Override
                public void notify(List<SalukiURL> urls) {
                    List<ResolvedServerInfo> servers = cacheProviderUrl(subscribeUrl, urls);
                    notifyLoadBalance(subscribeUrl, servers);
                }

            });
        }

        public void discover(SalukiURL subscribeUrl) {
            List<SalukiURL> urls = registry.discover(subscribeUrl);
            List<ResolvedServerInfo> servers = cacheProviderUrl(subscribeUrl, urls);
            notifyLoadBalance(subscribeUrl, servers);
        }

        public void remove(SalukiURL subscribeUrl, ResolvedServerInfo serverInfo) {
            List<ResolvedServerInfo> serverInfos = resolvedServerInfos.getIfPresent(subscribeUrl);
            List<ResolvedServerInfo> newServerInfos = Lists.newArrayList();
            newServerInfos.addAll(serverInfos);
            newServerInfos.remove(serverInfo);
            notifyLoadBalance(subscribeUrl, newServerInfos);
        }

        private void notifyLoadBalance(SalukiURL subscribeUrl, List<ResolvedServerInfo> servers) {
            NameResolver.Listener listener = resolvedListeners.getIfPresent(subscribeUrl);
            if (servers != null && servers.isEmpty() && listener != null) {
                listener.onUpdate(Collections.singletonList(servers), Attributes.EMPTY);
            } else {
                listener.onError(Status.NOT_FOUND.withDescription("There is no service registy in consul by"
                                                                  + subscribeUrl.toFullString()));
            }
        }

        private List<ResolvedServerInfo> cacheProviderUrl(SalukiURL subscribeUrl, List<SalukiURL> urls) {
            if (urls != null && !urls.isEmpty()) {
                List<ResolvedServerInfo> servers = new ArrayList<ResolvedServerInfo>(urls.size());
                for (int i = 0; i < urls.size(); i++) {
                    SalukiURL url = urls.get(i);
                    String ip = url.getHost();
                    int port = url.getPort();
                    servers.add(new ResolvedServerInfo(new InetSocketAddress(InetAddresses.forString(ip), port),
                                                       Attributes.EMPTY));
                }
                resolvedServerInfos.put(subscribeUrl, servers);
                return servers;
            }
            return null;
        }
    }

}
