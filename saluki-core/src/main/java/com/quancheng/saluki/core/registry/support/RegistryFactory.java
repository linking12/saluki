package com.quancheng.saluki.core.registry.support;

import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.registry.Registry;

import io.grpc.Internal;

@Internal
public abstract class RegistryFactory {

    public abstract Registry newRegistry(SalukiURL url);
}
