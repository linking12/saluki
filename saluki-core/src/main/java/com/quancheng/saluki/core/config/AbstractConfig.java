package com.quancheng.saluki.core.config;

import java.io.Serializable;

import com.google.common.base.Preconditions;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.GRPCEngine;
import com.quancheng.saluki.core.grpc.GRPCEngineImpl;

public class AbstractConfig implements Serializable {

    private static final long               serialVersionUID = 5736580957909744603L;

    // 注册配置名称
    protected String                        registryName;

    // 注册中心地址
    protected String                        registryAddress;

    // 注册中心缺省端口
    protected Integer                       registryPort;

    // 扩展配置点
    protected ExtendConfig                  extConfig;

    // 应用名称
    protected String                        application;

    // 分组
    protected String                        group;

    // 服务版本
    protected String                        version;

    // 拦截器
    protected String                        interceptor;

    protected transient volatile GRPCEngine grpcEngine;

    public String getRegistryName() {
        return registryName;
    }

    public void setRegistryName(String registryName) {
        this.registryName = registryName;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public Integer getRegistryPort() {
        return registryPort;
    }

    public void setRegistryPort(Integer registryPort) {
        this.registryPort = registryPort;
    }

    public ExtendConfig getExtConfig() {
        return extConfig;
    }

    public void setExtConfig(ExtendConfig extConfig) {
        this.extConfig = extConfig;
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

    public String getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(String interceptor) {
        this.interceptor = interceptor;
    }

    protected void loadRegistry() {
        if (grpcEngine == null) {
            Preconditions.checkNotNull(registryAddress, "registryAddress (%s) is not Null");
            Preconditions.checkNotNull(registryPort, "registryPort (%s) is not Null");
            String registryName = this.registryName != null ? this.registryName : SalukiConstants.REGISTRY_PROTOCOL;
            SalukiURL registryUrl = new SalukiURL(registryName, this.registryAddress, this.registryPort);
            grpcEngine = new GRPCEngineImpl(registryUrl);
        }
    }

}
