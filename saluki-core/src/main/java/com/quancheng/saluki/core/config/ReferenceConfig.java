package com.quancheng.saluki.core.config;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
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
    private Boolean                   generic;

    // 是否是injvm调用
    private Boolean                   injvm;

    // 是否异步
    private Boolean                   async;

    // 请求超时时间
    private Integer                   requestTimeout;

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

    public void setVersion(String version) {
        this.version = version;
    }

    public synchronized Object get() {
        if (ref == null) {
            init();
        }
        return ref;
    }

    private void init() {
        checkParam();
        loadRegistry();
        try {
            ref = grpcEngine.getProxy(buildRefUrl());
        } catch (Exception e) {
            throw new IllegalStateException("Create proxy failed ", e);
        }
    }

    private void checkParam() {
        Preconditions.checkNotNull(interfaceName, "interfaceName (%s) is not Null");
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
        } else {
            params.put(SalukiConstants.GRPC_IN_LOCAL_PROCESS, Boolean.FALSE.toString());
        }
        if (!this.async) {
            params.put(SalukiConstants.RPCTYPE_KEY, Integer.valueOf(SalukiConstants.RPCTYPE_ASYNC).toString());
        }
        if (this.generic) {
            params.put(SalukiConstants.GENERIC_KEY, Boolean.TRUE.toString());
        } else {
            params.put(SalukiConstants.GENERIC_KEY, Boolean.FALSE.toString());
        }
        if (StringUtils.isNotBlank(this.group)) {
            params.put(SalukiConstants.GROUP_KEY, this.group);
        } else {
            if (StringUtils.isNotBlank(this.application)) {
                params.put(SalukiConstants.GROUP_KEY, this.application);
            } else {
                params.put(SalukiConstants.GROUP_KEY, SalukiConstants.DEFAULT_GROUP);
            }
        }
        if (StringUtils.isNotBlank(this.version)) {
            params.put(SalukiConstants.VERSION_KEY, version);
        } else {
            params.put(SalukiConstants.VERSION_KEY, SalukiConstants.DEFAULT_VERSION);
        }
        if (this.requestTimeout != 0) {
            params.put(SalukiConstants.RPCTIMEOUT_KEY, this.requestTimeout.toString());
        }
        String interfaceName = (interfaceClass != null
                                && interfaceClass.getName() != null) ? interfaceClass.getName() : this.interfaceName;
        SalukiURL refUrl = new SalukiURL(SalukiConstants.DEFATULT_PROTOCOL, NetUtils.getLocalHost(), 0, interfaceName,
                                         params);
        return refUrl;
    }

}
