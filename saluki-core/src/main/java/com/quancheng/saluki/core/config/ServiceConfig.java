package com.quancheng.saluki.core.config;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.SalukiServer;
import com.quancheng.saluki.core.utils.NetUtils;

public class ServiceConfig extends BasicConfig {

    private static final long               serialVersionUID = 1L;

    // 服务暴露端口
    private int                             port;

    // 服务接口
    private Set<InterfaceConfig>            serviceConigs    = Sets.newConcurrentHashSet();

    private transient volatile SalukiServer server;

    public void setPort(int port) {
        this.port = port;
    }

    public void destroy() {
        server.shutDown();
    }

    public void addServiceConfig(String serviceName, String group, String version, Object instance) {
        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setServiceName(serviceName);
        interfaceConfig.setGroup(group);
        interfaceConfig.setVersion(version);
        interfaceConfig.setRef(instance);
        serviceConigs.add(interfaceConfig);
    }

    public synchronized void export() {
        loadRegistry();
        Map<SalukiURL, Object> providerUrls = Maps.newHashMap();
        for (InterfaceConfig config : serviceConigs) {
            // 服务名称
            String protocol = config.getServiceName();
            // 服务引用
            Object protocolImpl = config.getRef();
            Map<String, String> params = Maps.newHashMap();
            if (StringUtils.isBlank(config.getGroup())) {
                if (StringUtils.isNoneBlank(this.application)) {
                    params.put(SalukiConstants.GROUP_KEY, this.application);
                }
            } else {
                params.put(SalukiConstants.GROUP_KEY, config.getGroup());
            }
            if (StringUtils.isNotBlank(config.getVersion())) {
                params.put(SalukiConstants.VERSION_KEY, config.getVersion());
            }
            String localIp = System.getProperty("grpc.serverIp", NetUtils.getLocalHost());
            SalukiURL providerUrl = new SalukiURL(SalukiConstants.DEFATULT_PROTOCOL, localIp, port, protocol, params);
            providerUrls.put(providerUrl, protocolImpl);
        }
        try {
            SalukiServer server = grpcEngine.getServer(providerUrls, port);
            this.server = server;
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

}
