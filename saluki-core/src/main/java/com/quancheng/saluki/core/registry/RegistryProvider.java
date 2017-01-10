/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.registry;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Lists;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.registry.internal.RegistryFactory;

import io.grpc.Internal;

/**
 * @author shimingliu 2016年12月14日 下午1:48:47
 * @version RegistryProvider.java, v 0.0.1 2016年12月14日 下午1:48:47 shimingliu
 */
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
        public Registry newRegistry(GrpcURL url) {
            url = url.setPath(Registry.class.getName())//
                     .addParameter(Constants.INTERFACE_KEY, Registry.class.getName());
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
