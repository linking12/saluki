/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.springsupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.quancheng.saluki.springsupport.internal.ConfigBean;

/**
 * @author shimingliu 2017年3月1日 上午9:31:41
 * @version ConfigBeanDefinitionParser.java, v 0.0.1 2017年3月1日 上午9:31:41 shimingliu
 */
public class ConfigBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }

    @Override
    protected Class<?> getBeanClass(Element element) {
        return ConfigBean.class;
    }

    @Override
    protected String getBeanClassName(Element element) {
        return "com.quancheng.saluki.springsupport.internal.ConfigBean";
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        String application = element.getAttribute("application");
        String registry = element.getAttribute("registry");
        String rpcPort = element.getAttribute("rpcPort");
        String httpPort = element.getAttribute("httpPort");
        if (StringUtils.hasText(application)) {
            bean.addPropertyValue("application", application);
        }
        if (StringUtils.hasText(registry)) {
            bean.addPropertyValue("registryAddress", registry);
        }
        if (StringUtils.hasText(rpcPort)) {
            bean.addPropertyValue("realityRpcPort", Integer.valueOf(rpcPort));
        }
        if (StringUtils.hasText(httpPort)) {
            bean.addPropertyValue("httpPort", Integer.valueOf(httpPort));
        }
    }

}
