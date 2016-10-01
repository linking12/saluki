package com.quancheng.saluki.core.registry;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Lists;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.registry.support.RegistryFactory;

import io.grpc.Internal;

@Internal
public abstract class RegistryProvider extends RegistryFactory {

    private static final List<RegistryProvider> registryProviders = load();

    private static final RegistryFactory        internalFactory   = new InternalRegistryFactory(registryProviders);

    static List<RegistryProvider> load() {
        Iterable<RegistryProvider> candidates = ServiceLoader.load(RegistryProvider.class, getCorrectClassLoader());
        List<RegistryProvider> list = Lists.newArrayList();
        for (RegistryProvider current : candidates) {
            if (!current.isAvailable()) {
                continue;
            }
            list.add(current);
        }
        Collections.sort(list, Collections.reverseOrder(new Comparator<RegistryProvider>() {

            @Override
            public int compare(RegistryProvider provider1, RegistryProvider provider2) {
                return provider1.priority() - provider2.priority();
            }
        }));
        return Collections.unmodifiableList(list);
    }

    protected abstract boolean isAvailable();

    protected abstract int priority();

    public static RegistryFactory asFactory() {
        return internalFactory;
    }

    private static ClassLoader getCorrectClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static class InternalRegistryFactory extends RegistryFactory {

        private final List<RegistryProvider>       providers;
        private static final ReentrantLock         LOCK       = new ReentrantLock();
        private static final Map<String, Registry> REGISTRIES = new ConcurrentHashMap<String, Registry>();

        public InternalRegistryFactory(List<RegistryProvider> providers){
            this.providers = providers;
        }

        @Override
        public Registry newRegistry(SalukiURL url) {
            url = url.setPath(Registry.class.getName())//
                     .addParameter(SalukiConstants.INTERFACE_KEY, Registry.class.getName());
            String key = url.toServiceString();
            LOCK.lock();
            try {
                Registry registry = REGISTRIES.get(key);
                if (registry != null) {
                    return registry;
                }
                for (RegistryProvider provider : providers) {
                    registry = provider.newRegistry(url);
                    if (registry != null) {
                        continue;
                    }
                }
                if (registry == null) {
                    throw new IllegalStateException("Can not create registry " + url);
                }
                REGISTRIES.put(key, registry);
                return registry;
            } finally {
                LOCK.unlock();
            }
        }

    }
}
