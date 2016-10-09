package com.quancheng.saluki.core.config;

import java.util.HashMap;
import java.util.Map;

import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.GRPCEngine;
import com.quancheng.saluki.core.grpc.GRPCEngineImpl;
import com.quancheng.saluki.core.utils.ReflectUtil;

public class ReferenceConfig<T> extends AbstractReferenceConfig {

    private static final long          serialVersionUID = -9023239057692247223L;

    // 接口类型
    private String                     interfaceName;

    private Class<?>                   interfaceClass;

    // 接口代理类引用
    private transient volatile T       ref;

    private transient GRPCEngine       grpcEngin;

    private transient volatile boolean initialized;

    private transient volatile boolean destroyed;

    public ReferenceConfig(){
    }

    public synchronized T get() {
        if (destroyed) {
            throw new IllegalStateException("Already destroyed!");
        }
        if (ref == null) {
            init();
        }
        return ref;
    }

    private void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (interfaceName == null || interfaceName.length() == 0) {
            throw new IllegalStateException("<saluki:reference interface=\"\" /> interface not allow null!");
        }
        try {
            interfaceClass = ReflectUtil.name2class(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        checkInterfaceAndMethods(interfaceClass, methods);
        SalukiURL registryUrl = loadRegistryUrl();
        grpcEngin = new GRPCEngineImpl(registryUrl);
        Map<String, String> params = new HashMap<String, String>();
        // SalukiURL refUrl = new SalukiURL(protocol.getName(), localIp, MotanConstants.DEFAULT_INT_VALUE,
        // interfaceClass.getName(), params);

    }

}
