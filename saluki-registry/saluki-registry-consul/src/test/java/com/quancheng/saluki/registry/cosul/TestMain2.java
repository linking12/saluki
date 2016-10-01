package com.quancheng.saluki.registry.cosul;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.registry.consul.ConsulRegistry;

public class TestMain2 {

    public static void main(String[] args) {
        SalukiURL registryUrl = new SalukiURL(SalukiConstants.REGISTRY_PROTOCOL, "192.168.99.101", 8500);
        Registry consulRegisty = new ConsulRegistry(registryUrl);
        SalukiURL subscribeUrl = new SalukiURL(SalukiConstants.DEFATULT_PROTOCOL, "127.0.0.3", 12201,
                                               "com.quancheng.test.service");
        consulRegisty.register(subscribeUrl);
        synchronized (TestMain.class) {
            while (true) {
                try {
                    TestMain2.class.wait();
                } catch (Throwable e) {
                }
            }
        }

    }

}
