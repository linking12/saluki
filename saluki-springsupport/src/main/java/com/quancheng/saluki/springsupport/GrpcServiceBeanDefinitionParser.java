/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.springsupport;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.quancheng.saluki.springsupport.internal.ServiceBean;

/**
 * @author shimingliu 2017年3月1日 下午12:47:27
 * @version GrpcServiceBeanDefinitionParser.java, v 0.0.1 2017年3月1日 下午12:47:27 shimingliu
 */
public class GrpcServiceBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String SERVICEBEAN_INTERFACE = "interface";

    private static final String SERVICEBEAN_GROUP     = "group";

    private static final String SERVICEBEAN_VERSION   = "version";

    private static final String SERVICEBEAN_REF       = "ref";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServiceBean.class);
        this.setInterface(element, builder);
        this.setGroup(element, builder);
        this.setVersion(element, builder);
        this.setRef(element, builder);
        return builder.getBeanDefinition();
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

    private void setRef(Element element, BeanDefinitionBuilder builder) {
        String ref = element.getAttribute(SERVICEBEAN_REF);
        if (StringUtils.hasText(ref)) {
            builder.addPropertyReference("ref", ref);
        }
    }

    @Override
    protected boolean shouldGenerateIdAsFallback() {
        return true;
    }

}
