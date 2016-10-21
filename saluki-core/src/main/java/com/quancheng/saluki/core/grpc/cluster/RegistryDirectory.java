package com.quancheng.saluki.core.grpc.cluster;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
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

        private static RegistryDirectory              directory;

        private Map<String, List<ResolvedServerInfo>> resolvedServerInfos;

        private Map<String, NameResolver.Listener>    resolvedListeners;

        private Registry                              registry;

        public void init(SalukiURL registryUrl) {
            this.registry = RegistryProvider.asFactory().newRegistry(registryUrl);
            this.resolvedServerInfos = Maps.newConcurrentMap();
            this.resolvedListeners = Maps.newConcurrentMap();
            directory = this;
        }

        public void subscribe(SalukiURL subscribeUrl, NameResolver.Listener listener) {
            resolvedListeners.put(subscribeUrl.getServiceKey(), listener);
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
            List<ResolvedServerInfo> serverInfos = resolvedServerInfos.get(subscribeUrl.getServiceKey());
            List<ResolvedServerInfo> newServerInfos = Lists.newArrayList();
            newServerInfos.addAll(serverInfos);
            newServerInfos.remove(serverInfo);
            notifyLoadBalance(subscribeUrl, newServerInfos);
        }

        private void notifyLoadBalance(SalukiURL subscribeUrl, List<ResolvedServerInfo> servers) {
            NameResolver.Listener listener = resolvedListeners.get(subscribeUrl.getServiceKey());
            if (servers != null && !servers.isEmpty()) {
                listener.onUpdate(Collections.singletonList(servers), Attributes.EMPTY);
            } else {
                if (listener == null) {
                    throw new IllegalArgumentException("Have not start in SalukiNameResolver");
                }
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
                resolvedServerInfos.put(subscribeUrl.getServiceKey(), servers);
                return servers;
            }
            return null;
        }
    }

}
