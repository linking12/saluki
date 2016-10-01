package com.quancheng.saluki.core.config;

import java.util.List;

import com.quancheng.saluki.core.common.SalukiURL;

public class AbstractInterfaceConfig extends AbstractConfig {

    private static final long      serialVersionUID = -356407161276575752L;

    // 注册中心的配置列表
    protected List<RegistryConfig> registries;

    // 扩展配置点
    protected ExtendConfig         extConfig;

    // 应用名称
    protected String               application;

    // 分组
    protected String               group;

    // 服务版本
    protected String               version;

    // 拦截器
    protected String               interceptor;

    // 是否注册
    protected Boolean              register;

    // 是原生Grpc方式还是代理方式
    protected Boolean              isGrpc;

    // 方法配置
    protected List<MethodConfig>   methods;

    public List<RegistryConfig> getRegistries() {
        return registries;
    }

    public void setRegistries(List<RegistryConfig> registries) {
        this.registries = registries;
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

    public Boolean getRegister() {
        return register;
    }

    public void setRegister(Boolean register) {
        this.register = register;
    }

    public Boolean getIsGrpc() {
        return isGrpc;
    }

    public void setIsGrpc(Boolean isGrpc) {
        this.isGrpc = isGrpc;
    }

    public List<MethodConfig> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodConfig> methods) {
        this.methods = methods;
    }

    protected List<SalukiURL> loadRegistryUrls() {
        return null;
    }

    protected void checkInterfaceAndMethods(Class<?> interfaceClass, List<MethodConfig> methods) {

    }
}
