/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.router;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.registry.NotifyListener;
import com.quancheng.saluki.core.registry.Registry;

import io.grpc.ResolvedServerInfo;

/**
 * @author shimingliu 2016年12月28日 下午8:33:41
 * @version GrpcRouterFactory.java, v 0.0.1 2016年12月28日 下午8:33:41 shimingliu
 */
public final class GrpcRouterFactory {

    private static final Map<String, String> routerMessages = Maps.newConcurrentMap();

    private static final GrpcRouterFactory   instance       = new GrpcRouterFactory();

    private GrpcRouterFactory(){
    }

    private static final NotifyListener.NotifyRouterListener routerListener = new NotifyListener.NotifyRouterListener() {

        @Override
        public void notify(String group, String routerCondition) {
            if (routerCondition == null) {
                routerMessages.remove(group);
            } else {
                routerMessages.put(group, routerCondition);
            }
        }

    };

    public static GrpcRouterFactory getInstance() {
        return instance;
    }

    public static void configWithRegistry(Registry registry, GrpcURL subscribeUrl) {
        registry.subscribe(subscribeUrl.getGroup(), routerListener);
    }

    public static GrpcRouter createRouter(GrpcURL subscribeUrl) {
        String group = subscribeUrl.getGroup();
        String routerMessage = routerMessages.get(group);
        if (routerMessage.startsWith("condition://")) {
            routerMessage = routerMessage.replaceAll("condition://", "");
            return new ConditionRouter(subscribeUrl, routerMessage);
        } else {
            routerMessage = routerMessage.replaceAll("script://", "");
            return new ScriptRouter(subscribeUrl, routerMessage);
        }
    }

    private static class ConditionRouter extends GrpcRouter {

        public ConditionRouter(GrpcURL url, String routerMessage){
            super(url, routerMessage);
        }

        @Override
        List<? extends List<ResolvedServerInfo>> router(List<ResolvedServerInfo> servers) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        void parseRouter() {
            // TODO Auto-generated method stub

        }

    }

    private static class ScriptRouter extends GrpcRouter {

        public ScriptRouter(GrpcURL url, String routerMessage){
            super(url, routerMessage);
        }

        @Override
        List<? extends List<ResolvedServerInfo>> router(List<ResolvedServerInfo> servers) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        void parseRouter() {
            // TODO Auto-generated method stub

        }

    }

}
