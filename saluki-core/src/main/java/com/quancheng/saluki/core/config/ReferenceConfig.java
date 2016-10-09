package com.quancheng.saluki.core.config;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.GRPCEngine;
import com.quancheng.saluki.core.grpc.GRPCEngineImpl;
import com.quancheng.saluki.core.utils.NetUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;

public class ReferenceConfig extends AbstractConfig {

    private static final long           serialVersionUID = -9023239057692247223L;

    // 接口类型
    private String                      interfaceName;

    // 是否使用泛接口
    protected Boolean                   generic;

    // 是否是injvm调用
    protected Boolean                   injvm;

    // 是否异步
    protected Boolean                   async;

    // 请求超时时间
    protected Integer                   requestTimeout;

    private transient volatile Object   ref;

    private transient volatile Class<?> interfaceClass;

    public ReferenceConfig(){
    }

    public synchronized Object get() {
        if (ref == null) {
            init();
        }
        return ref;
    }

    private void init() {
        checkParam();
        SalukiURL registryUrl = loadRegistryUrl();
        GRPCEngine grpcEngin = new GRPCEngineImpl(registryUrl);
        try {
            ref = grpcEngin.getProxy(buildRefUrl()).getProxy();
        } catch (Exception e) {
            throw new IllegalStateException("Create proxy failed ", e);
        }

    }

    private void checkParam() {
        if (StringUtils.isBlank(this.interfaceName)) {
            throw new IllegalStateException("<saluki:reference interface=\"\" /> interface not allow null!");
        }
        if (!this.generic) {
            try {
                interfaceClass = ReflectUtil.name2class(interfaceName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

    }

    private SalukiURL buildRefUrl() {
        Map<String, String> params = Maps.newHashMap();
        if (this.injvm) {
            params.put(SalukiConstants.GRPC_IN_LOCAL_PROCESS, Boolean.TRUE.toString());
        }
        if (!this.async) {
            params.put(SalukiConstants.RPCTYPE_KEY, Integer.valueOf(SalukiConstants.RPCTYPE_ASYNC).toString());
        }
        if (this.generic) {
            params.put(SalukiConstants.GENERIC_KEY, Boolean.TRUE.toString());
        }
        if (this.group == null) {
            if (this.application != null) {
                params.put(SalukiConstants.GROUP_KEY, this.application);
            } else {
                params.put(SalukiConstants.GROUP_KEY, SalukiConstants.DEFAULTGROUP);
            }
        }
        if (this.version == null) {
            params.put(SalukiConstants.VERSION_KEY, SalukiConstants.DEFAULTVERSION);
        }
        if (this.requestTimeout != 0) {
            params.put(SalukiConstants.RPCTIMEOUT_KEY, this.requestTimeout.toString());
        }
        String interfaceName = interfaceClass.getName() != null ? interfaceClass.getName() : this.interfaceName;
        SalukiURL refUrl = new SalukiURL(SalukiConstants.DEFATULT_PROTOCOL, NetUtils.getLocalHost(), 0, interfaceName,
                                         params);
        return refUrl;
    }

}
