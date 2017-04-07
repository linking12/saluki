/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.grpc;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.quancheng.saluki.gateway.grpc.service.ApiJarService;

/**
 * @author shimingliu 2017年4月7日 下午5:27:26
 * @version DefaultGrpcConfiguration.java, v 0.0.1 2017年4月7日 下午5:27:26 shimingliu
 */
@Configuration
@Profile("default-user-and-roles_route")
public class DefaultGrpcConfiguration implements InitializingBean {

    private static final String DEFAULT_API_URL     = "http://repo.quancheng-ec.com/repository/maven-snapshots/com/quancheng/shared/shared.api/1.1.6-SNAPSHOT/shared.api-1.1.6-20170104.091634-32.jar";

    private static final String DEFAULT_API_VERSION = "1.1.6-SNAPSHOT";

    @Autowired
    private ApiJarService       apiJarService;

    @Override
    public void afterPropertiesSet() throws Exception {
        apiJarService.saveJar(DEFAULT_API_VERSION, DEFAULT_API_URL);
    }

}
