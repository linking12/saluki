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
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.BindableService;

public class ServiceConfig extends BasicConfig {

    private static final long    serialVersionUID = 1L;

    // 服务暴露端口
    private int                  port;

    private Set<InterfaceConfig> serviceConigs    = Sets.newConcurrentHashSet();

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void addServiceConfig(String interfaceName, String group, String version, Object instance) {
        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setInterfaceName(interfaceName);
        interfaceConfig.setGroup(group);
        interfaceConfig.setVersion(version);
        interfaceConfig.setRef(instance);
        if (instance instanceof BindableService) {
            interfaceConfig.setGrpcStub(true);
        }
        Set<String> instanceInterfaces = Sets.newHashSet();
        for (Class<?> clzz : instance.getClass().getInterfaces()) {
            instanceInterfaces.add(clzz.getName());
        }
        if (!instanceInterfaces.contains(interfaceName)) {
            interfaceConfig.setGeneric(true);
        }
        serviceConigs.add(interfaceConfig);
    }

    public synchronized void export() {
        loadRegistry();
        Map<SalukiURL, Object> providerUrls = Maps.newHashMap();
        for (InterfaceConfig config : serviceConigs) {
            checkParam(config);
            // 服务名称
            String path = config.getInterfaceName();
            // 服务引用
            Object protocolImpl = config.getRef();
            Map<String, String> params = Maps.newHashMap();
            if (StringUtils.isBlank(config.getGroup())) {
                if (this.application != null) {
                    params.put(SalukiConstants.GROUP_KEY, this.application);
                } else {
                    params.put(SalukiConstants.GROUP_KEY, SalukiConstants.DEFAULT_GROUP);
                }
            } else {
                params.put(SalukiConstants.GROUP_KEY, config.getGroup());
            }
            if (StringUtils.isNotBlank(config.getVersion())) {
                params.put(SalukiConstants.VERSION_KEY, config.getVersion());
            } else {
                params.put(SalukiConstants.VERSION_KEY, SalukiConstants.DEFAULT_VERSION);
            }
            if (config.getGeneric()) {
                params.put(SalukiConstants.GENERIC_KEY, Boolean.TRUE.toString());
            }
            SalukiURL providerUrl = new SalukiURL(SalukiConstants.DEFATULT_PROTOCOL, NetUtils.getLocalHost(), port,
                                                  path, params);
            providerUrls.put(providerUrl, protocolImpl);
        }
        try {
            SalukiServer server = grpcEngine.getServer(providerUrls, port);
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

    private void checkParam(InterfaceConfig config) {
        if (!config.getGeneric() && !config.getGrpcStub()) {
            try {
                Class<?> interfaceClass = ReflectUtil.name2class(config.getInterfaceName());
                if (!interfaceClass.isAssignableFrom(config.getRef().getClass())) {
                    throw new IllegalStateException("The class " + config.getRef().getClass().getName()
                                                    + " unimplemented interface " + config.getInterfaceName() + "!");
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

    }

}
