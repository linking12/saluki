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

import com.quancheng.saluki.springsupport.internal.ReferenceBean;

/**
 * @author shimingliu 2017年3月1日 上午11:57:24
 * @version GrpcReferenceBeanDefinitionParser.java, v 0.0.1 2017年3月1日 上午11:57:24 shimingliu
 */
public class GrpcReferenceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String SERVICEBEAN_INTERFACE = "interface";

    private static final String SERVICEBEAN_GROUP     = "group";

    private static final String SERVICEBEAN_VERSION   = "version";

    @Override
    protected String getBeanClassName(Element element) {
        return ReferenceBean.class.getName();
    }

    @Override
    protected Class<?> getBeanClass(Element element) {
        return ReferenceBean.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        this.setInterface(element, builder);
        this.setGroup(element, builder);
        this.setVersion(element, builder);
    }

    private void setInterface(Element element, BeanDefinitionBuilder builder) {
        String serviceName = element.getAttribute(SERVICEBEAN_INTERFACE);
        if (StringUtils.hasText(serviceName)) {
            builder.addPropertyValue("serviceName", serviceName);
        }
    }

    private void setGroup(Element element, BeanDefinitionBuilder builder) {
        String group = element.getAttribute(SERVICEBEAN_GROUP);
        if (StringUtils.hasText(group)) {
            builder.addPropertyValue("group", group);
        }
    }

    private void setVersion(Element element, BeanDefinitionBuilder builder) {
        String version = element.getAttribute(SERVICEBEAN_VERSION);
        if (StringUtils.hasText(version)) {
            builder.addPropertyValue("version", version);
        }
    }

    @Override
    protected boolean shouldGenerateIdAsFallback() {
        return true;
    }
}
