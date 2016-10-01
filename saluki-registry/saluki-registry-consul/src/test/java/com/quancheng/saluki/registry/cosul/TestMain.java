package com.quancheng.saluki.registry.cosul;

import java.util.List;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.registry.consul.ConsulRegistry;

public class TestMain {

    public static void main(String[] args) {
        SalukiURL registryUrl = new SalukiURL(SalukiConstants.REGISTRY_PROTOCOL, "192.168.99.101", 8500);
        Registry consulRegisty = new ConsulRegistry(registryUrl);
        SalukiURL subscribeUrl = new SalukiURL(SalukiConstants.DEFATULT_PROTOCOL, "127.0.0.1", 12201,
                                               "com.quancheng.test.service");
        Thread listener = new Thread() {

            @Override
            public void run() {
                consulRegisty.subscribe(subscribeUrl, new NotifyListener() {

                    @Override
                    public void notify(List<SalukiURL> urls) {
                        System.out.println(urls);
                    }

                });
            }
        };
        listener.start();
        consulRegisty.register(subscribeUrl);
        synchronized (TestMain.class) {
            while (true) {
                try {
                    TestMain.class.wait();
                } catch (Throwable e) {
                }
            }
        }

    }

}
