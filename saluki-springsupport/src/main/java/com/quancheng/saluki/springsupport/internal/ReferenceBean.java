/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.springsupport.internal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.quancheng.saluki.core.config.RpcReferenceConfig;
import com.quancheng.saluki.core.utils.ReflectUtils;

/**
 * @author shimingliu 2017年2月28日 下午6:28:03
 * @version ReferenceBean.java, v 0.0.1 2017年2月28日 下午6:28:03 shimingliu
 */
public class ReferenceBean extends RpcReferenceConfig implements FactoryBean<Object>, ApplicationContextAware {

    private static final long            serialVersionUID = 1L;

    private ConfigBean                   thrallProperties;

    private transient ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getObject() throws Exception {
        if (this.applicationContext != null) {
            ConfigBean configBean = applicationContext.getBean(ConfigBean.class);
            this.thrallProperties = configBean;
            super.setAsync(true);
            this.addRegistyAddress();
            this.addHttpPort();
            return getProxyObj();
        } else {
            throw new java.lang.IllegalArgumentException("spring container is not started, applicationContext is null ");
        }

    }

    @Override
    public Class<?> getObjectType() {
        Class<?> clzz = super.getServiceClass();
        String servcieName = super.getServiceName();
        if (clzz == null && servcieName != null) {
            clzz = ReflectUtils.forName(servcieName);
        }
        return clzz;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private void addHttpPort() {
        int httpPort = thrallProperties.getHttpPort();
        if (httpPort != 0) {
            setHttpPort(httpPort);
        } else {
            throw new java.lang.IllegalArgumentException("http port must be set in properties");
        }

    }

    private void addRegistyAddress() {
        String registryAddress = thrallProperties.getRegistryAddress();
        if (StringUtils.isBlank(registryAddress)) {
            throw new java.lang.IllegalArgumentException("registry address can not be null or empty");
        } else {
            String[] registryHostAndPort = StringUtils.split(registryAddress, ":");
            if (registryHostAndPort.length < 2) {
                throw new java.lang.IllegalArgumentException("the pattern of registry address is host:port");
            }
            setRegistryAddress(registryHostAndPort[0]);
            setRegistryPort(Integer.valueOf(registryHostAndPort[1]));
        }
    }

}
