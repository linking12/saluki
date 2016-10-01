package com.quancheng.saluki.core.config;

public class AbstractServiceConfig extends AbstractInterfaceConfig {

    private static final long serialVersionUID = -7878683982899095137L;
    // 延迟暴露
    protected Integer         delay;

    // 是否暴露
    protected Boolean         export;

    // 是否使用令牌
    protected String          token;

    // 访问日志
    protected String          accesslog;

    // 允许执行请求数
    private Integer           executes;

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Boolean getExport() {
        return export;
    }

    public void setExport(Boolean export) {
        this.export = export;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccesslog() {
        return accesslog;
    }

    public void setAccesslog(String accesslog) {
        this.accesslog = accesslog;
    }

    public Integer getExecutes() {
        return executes;
    }

    public void setExecutes(Integer executes) {
        this.executes = executes;
    }

}
