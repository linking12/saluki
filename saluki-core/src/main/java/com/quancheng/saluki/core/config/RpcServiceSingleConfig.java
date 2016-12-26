/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.config;

import java.io.Serializable;

/**
 * @author shimingliu 2016年12月14日 下午2:05:00
 * @version RpcServiceBaseConfig.java, v 0.0.1 2016年12月14日 下午2:05:00 shimingliu
 */
public class RpcServiceSingleConfig<T> implements Serializable {

    private static final long serialVersionUID = -1221063599438762021L;

    private String            serviceName;

    private String            group;

    private String            version;

    private T                 ref;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
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

}
