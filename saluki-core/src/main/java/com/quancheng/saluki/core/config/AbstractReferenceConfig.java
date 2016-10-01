package com.quancheng.saluki.core.config;

public class AbstractReferenceConfig extends AbstractInterfaceConfig {

    private static final long serialVersionUID = -1776611056621529740L;
    // 代理类型
    protected String          proxy;

    // 是否使用泛接口
    protected String          generic;

    // 优先从JVM内获取引用实例
    protected Boolean         injvm;

    // 重试次数
    protected Integer         retries;

    // 最大并发调用
    protected Integer         actives;

    // 是否异步
    protected Boolean         async;

    // 请求超时时间
    protected Integer         requestTimeout;

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getGeneric() {
        return generic;
    }

    public void setGeneric(String generic) {
        this.generic = generic;
    }

    public Boolean getInjvm() {
        return injvm;
    }

    public void setInjvm(Boolean injvm) {
        this.injvm = injvm;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getActives() {
        return actives;
    }

    public void setActives(Integer actives) {
        this.actives = actives;
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

}
