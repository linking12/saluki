package com.quancheng.saluki.core.config;

import java.util.Map;

import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.utils.NetUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;

public class ServiceConfig extends AbstractConfig {

    private static final long   serialVersionUID = -4275752504314752426L;

    private int                 port;
    // 是否使用泛接口
    private Boolean             generic;

    // 是否是原生Grpc服务
    private Boolean             grpcStub;

    private Map<String, Object> refs             = Maps.newConcurrentMap();

    // 添加需要暴露的服务
    public void addRef(String path, Object refParam) {
        Object ref = refs.get(path);
        if (ref != null) {
            throw new IllegalStateException(String.format("service has exported,Name is %s instance is %s", path, ref));
        } else {
            refs.put(path, refParam);
        }
    }

    public synchronized void export() {
        loadRegistry();
        Map<SalukiURL, Object> providerUrls = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : refs.entrySet()) {
            // 服务名称
            String path = entry.getKey();
            // 服务引用
            Object protocolImpl = entry.getValue();
            checkParam(path, protocolImpl);
            Map<String, String> params = Maps.newHashMap();
            SalukiURL providerUrl = new SalukiURL(SalukiConstants.DEFATULT_PROTOCOL, NetUtils.getLocalHost(), port,
                                                  path, params);
            providerUrls.put(providerUrl, protocolImpl);
        }
        try {
            io.grpc.Server server = grpcEngine.getServer(providerUrls, port);
            Thread awaitThread = new Thread() {

                @Override
                public void run() {
                    try {
                        server.awaitTermination();
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                }

            };
            awaitThread.setDaemon(false);
            awaitThread.start();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }

    private void checkParam(String path, Object instance) {
        if (path == null) {
            throw new IllegalStateException("ref not allow null!");
        }
        if (!this.generic && this.grpcStub) {
            try {
                Class<?> interfaceClass = ReflectUtil.name2class(path);
                if (!interfaceClass.isAssignableFrom(instance.getClass())) {
                    throw new IllegalStateException("The class " + instance.getClass().getName()
                                                    + " unimplemented interface " + path + "!");
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

    }

}
