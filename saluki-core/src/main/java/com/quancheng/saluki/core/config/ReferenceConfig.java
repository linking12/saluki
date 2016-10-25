package com.quancheng.saluki.core.config;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.utils.NetUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;

public class ReferenceConfig extends BasicConfig {

    private static final long         serialVersionUID = -9023239057692247223L;

    // 分组
    protected String                  group;

    // 服务版本
    protected String                  version;

    // 接口名
    private String                    interfaceName;

    // 接口类型
    private Class<?>                  interfaceClass;

    // 是否使用泛接口
    private boolean                   generic;

    // 是否是injvm调用
    private boolean                   injvm;

    // 原生grpc stub调用
    private boolean                   grpcStub;

    // 是否异步
    private boolean                   async;

    // 请求超时时间
    private int                       requestTimeout;

    // 方法签名
    private List<String>              methodNames;

    // 重试次数
    private int                       reties;

    private transient volatile Object ref;

    public ReferenceConfig(){
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public Boolean getGeneric() {
        return generic;
    }

    public void setGeneric(Boolean generic) {
        this.generic = generic;
    }

    public Boolean getInjvm() {
        return injvm;
    }

    public void setInjvm(Boolean injvm) {
        this.injvm = injvm;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public Boolean getGrpcStub() {
        return grpcStub;
    }

    public void setGrpcStub(Boolean grpcStub) {
        this.grpcStub = grpcStub;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getMethodNames() {
        return methodNames;
    }

    public void setMethodNames(List<String> methodNames) {
        this.methodNames = methodNames;
    }

    public int getReties() {
        return reties;
    }

    public void setReties(int reties) {
        this.reties = reties;
    }

    public synchronized Object get() {
        if (ref == null) {
            init();
        }
        return ref;
    }

    private void init() {
        loadRegistry();
        try {
            ref = grpcEngine.getProxy(buildRefUrl());
        } catch (Exception e) {
            throw new IllegalStateException("Create proxy failed ", e);
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
        if (this.grpcStub) {
            params.put(SalukiConstants.GRPC_STUB_KEY, Boolean.TRUE.toString());
        }
        if (StringUtils.isNotBlank(this.group)) {
            params.put(SalukiConstants.GROUP_KEY, this.group);
        } else {
            if (StringUtils.isNotBlank(this.application)) {
                params.put(SalukiConstants.GROUP_KEY, this.application);
            }
        }
        if (StringUtils.isNotBlank(this.version)) {
            params.put(SalukiConstants.VERSION_KEY, version);
        }
        if (this.requestTimeout != 0) {
            params.put(SalukiConstants.RPCTIMEOUT_KEY, Integer.valueOf(requestTimeout).toString());
        }
        if (!this.methodNames.isEmpty()) {
            params.put(SalukiConstants.METHODS_KEY, StringUtils.join(this.methodNames, ","));
        }
        if (this.reties != 0) {
            params.put(SalukiConstants.METHOD_RETRY_KEY, Integer.valueOf(this.reties).toString());
        }
        String interfaceClassName = interfaceClass != null ? interfaceClass.getName() : this.interfaceName;
        params.put(SalukiConstants.INTERFACECLASS_KEY, interfaceClassName);
        SalukiURL refUrl = new SalukiURL(SalukiConstants.DEFATULT_PROTOCOL, NetUtils.getLocalHost(), 0, interfaceName,
                                         params);
        return refUrl;
    }

}
