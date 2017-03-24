/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.filters.route;

import java.util.List;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.http.HttpServletRequestWrapper;
import com.quancheng.saluki.gateway.grpc.GrpcRemoteComponent;
import com.quancheng.saluki.gateway.storage.RouterLocalCache;
import com.quancheng.saluki.gateway.storage.support.ZuulRouteEntity;

/**
 * @author shimingliu 2017年3月23日 上午10:43:32
 * @version GrpcRemoteApiFilter.java, v 0.0.1 2017年3月23日 上午10:43:32 shimingliu
 */
public class GrpcRemoteApiFilter extends ZuulFilter {

    private GrpcRemoteComponent grpcRemote;

    public GrpcRemoteApiFilter(GrpcRemoteComponent grpcRemote){
        this.grpcRemote = grpcRemote;
    }

    @Override
    public boolean shouldFilter() {
        ZuulRouteEntity route = findRoute();
        return (route != null && route.getIs_grpc() != null) ? route.getIs_grpc() : false;
    }

    private ZuulRouteEntity findRoute() {
        RequestContext context = RequestContext.getCurrentContext();
        String requestPath = context.getRequest().getServletPath();
        List<ZuulRouteEntity> routes = RouterLocalCache.getInstance().getRouters();
        for (ZuulRouteEntity route : routes) {
            if (route.getPath().equals(requestPath)) {
                return route;
            }
        }
        return null;
    }

    @Override
    public Object run() {
        ZuulRouteEntity route = findRoute();
        String service = route.getService_name();
        String group = route.getGroup();
        String version = route.getVersion();
        String method = route.getMethod();
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequestWrapper request = (HttpServletRequestWrapper) ctx.getRequest();
        String grpcRequestJson = request.getParameter("requestBody");
        try {
            return grpcRemote.callRemoteService(service, group, version, method, grpcRequestJson);
        } catch (Throwable e) {
            ctx.set("error.status_code", 500);
            ctx.set("error.message", e.getMessage());
            ctx.set("error.exception", e);
        }
        return null;
    }

    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

}
