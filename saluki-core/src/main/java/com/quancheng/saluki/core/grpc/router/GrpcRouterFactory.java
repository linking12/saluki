/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.router;

import java.util.List;

import com.quancheng.saluki.core.common.GrpcURL;

import io.grpc.ResolvedServerInfo;

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

    public GrpcRouter createRouter(GrpcURL refUrl, String routerMessage) {
        if (routerMessage.startsWith("condition://")) {
            routerMessage = routerMessage.replaceAll("condition://", "");
            return new ConditionRouter(refUrl, routerMessage);
        } else {
            routerMessage = routerMessage.replaceAll("script://", "");
            return new ScriptRouter(refUrl, routerMessage);
        }
    }

    private class ConditionRouter extends GrpcRouter {

        public ConditionRouter(GrpcURL url, String routerMessage){
            super(url, routerMessage);
        }

        @Override
        void parseRouter() {

        }

        @Override
        public List<? extends List<ResolvedServerInfo>> router(List<? extends List<ResolvedServerInfo>> servers) {
            return null;
        }

    }

    private class ScriptRouter extends GrpcRouter {

        public ScriptRouter(GrpcURL url, String routerMessage){
            super(url, routerMessage);
        }

        @Override
        void parseRouter() {

        }

        @Override
        public List<? extends List<ResolvedServerInfo>> router(List<? extends List<ResolvedServerInfo>> servers) {
            return null;
        }

    }

}
