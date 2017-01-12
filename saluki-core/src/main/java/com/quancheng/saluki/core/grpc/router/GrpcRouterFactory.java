/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.router;

import org.apache.commons.lang3.StringUtils;

import com.quancheng.saluki.core.grpc.router.internal.ConditionRouter;
import com.quancheng.saluki.core.grpc.router.internal.ScriptRouter;

/**
 * @author shimingliu 2016年12月28日 下午8:33:41
 * @version GrpcRouterFactory.java, v 0.0.1 2016年12月28日 下午8:33:41 shimingliu
 */
public final class GrpcRouterFactory {

    private static final GrpcRouterFactory instance = new GrpcRouterFactory();

    private GrpcRouterFactory(){
    }

    public static GrpcRouterFactory getInstance() {
        return instance;
    }

    public GrpcRouter createRouter(String routerMessage) {
        if (!routerMessage.startsWith("condition://")) {
            String[] router = StringUtils.split(routerMessage, "://");
            if (router.length == 2) {
                String type = router[0];
                String routerScript = router[1];
                return new ScriptRouter(type, routerScript);
            }
            throw new IllegalStateException(new IllegalStateException("No router type for script"));
        } else {
            routerMessage = routerMessage.replaceAll("condition://", "");
            return new ConditionRouter(routerMessage);
        }
    }
}
