package com.quancheng.saluki.core.config;

import java.io.Serializable;

import com.google.common.base.Preconditions;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.GRPCEngine;

public class BasicConfig implements Serializable {

    private static final long               serialVersionUID = 5736580957909744603L;

    // 应用名称
    protected String                        application;

    // 注册配置名称
    protected String                        registryName;

    // 注册中心地址
    protected String                        registryAddress;

    // 注册中心缺省端口
    protected Integer                       registryPort;

    // 扩展配置点
    protected ExtendConfig                  extConfig;

    // 拦截器
    protected String                        interceptor;

    // 监控统计时间间隔
    protected String                        monitorinterval;

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

    public String getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(String interceptor) {
        this.interceptor = interceptor;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getMonitorinterval() {
        return monitorinterval;
    }

    public void setMonitorinterval(String monitorinterval) {
        this.monitorinterval = monitorinterval;
    }

    protected void loadRegistry() {
        if (grpcEngine == null) {
            Preconditions.checkNotNull(registryAddress, "registryAddress  is not Null", registryAddress);
            Preconditions.checkState(registryPort != 0, "RegistryPort can not be zero", registryPort);
            String registryName = this.registryName != null ? this.registryName : SalukiConstants.REGISTRY_PROTOCOL;
            SalukiURL registryUrl = new SalukiURL(registryName, this.registryAddress, this.registryPort);
            grpcEngine = new GRPCEngine(registryUrl);
        }
    }

}
