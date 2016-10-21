package com.quancheng.saluki.registry.consul;

import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.cluster.RegistryDirectory;

public class TestMain {

    public static void main(String[] args) {

        RegistryDirectory dirty = new RegistryDirectory.Default();
        SalukiURL url = new SalukiURL("consul", "192.168.99.101", 8500);
        dirty.init(url);

        System.out.println(RegistryDirectory.getInstance());
    }
}
