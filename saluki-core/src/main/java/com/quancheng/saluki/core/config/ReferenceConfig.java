package com.quancheng.saluki.core.config;

import java.lang.reflect.Proxy;
import java.util.Map;

import com.google.common.collect.Maps;

public class ReferenceConfig<T> extends AbstractReferenceConfig {

    private static final long          serialVersionUID = -9023239057692247223L;

    // 接口类型
    private String                     interfaceName;

    private Class<?>                   interfaceClass;

    // 接口代理类引用
    private transient volatile T       ref;

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
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        checkInterfaceAndMethods(interfaceClass, methods);
        if (!getIsGrpc()) {

        } else {

        }
    }

    private T createProxy(Map<String, String> map) {
        return null;
    }

}
