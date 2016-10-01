package com.quancheng.saluki.registry.consul;

import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.core.registry.RegistryProvider;

public class ConsulRegistryProvider extends RegistryProvider {

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 0;
    }

    @Override
    public Registry newRegistry(SalukiURL url) {
        return new ConsulRegistry(url);
    }

}
