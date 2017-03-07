/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.springsupport;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author shimingliu 2017年2月28日 下午6:24:45
 * @version SalukiNamespaceHandler.java, v 0.0.1 2017年2月28日 下午6:24:45 shimingliu
 */
public class GrpcNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
        registerBeanDefinitionParser("referer", new GrpcReferenceBeanDefinitionParser());
        registerBeanDefinitionParser("service", new GrpcServiceBeanDefinitionParser());
    }

}
