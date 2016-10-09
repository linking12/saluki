package com.quancheng.saluki.core.config;

import java.io.Serializable;

import com.quancheng.saluki.core.common.SalukiURL;

public class AbstractConfig implements Serializable {

    private static final long serialVersionUID = 5736580957909744603L;

    protected String          id;

    // 注册中心的配置列表
    protected RegistryConfig  registryConfig;

    // 扩展配置点
    protected ExtendConfig    extConfig;

    // 应用名称
    protected String          application;

    // 分组
    protected String          group;

    // 服务版本
    protected String          version;

    // 拦截器
    protected String          interceptor;

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected SalukiURL loadRegistryUrl() {
        return null;
    }

}
