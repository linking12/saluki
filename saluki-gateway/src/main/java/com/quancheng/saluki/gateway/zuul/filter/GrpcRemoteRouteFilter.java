/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.zuul.filter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.quancheng.saluki.gateway.grpc.service.GrpcRemoteComponent;
import com.quancheng.saluki.gateway.zuul.dto.ZuulRouteDto;
import com.quancheng.saluki.gateway.zuul.extend.RouterLocalCache;

/**
 * @author shimingliu 2017年3月23日 上午10:43:32
 * @version GrpcRemoteApiFilter.java, v 0.0.1 2017年3月23日 上午10:43:32 shimingliu
 */
public class GrpcRemoteRouteFilter extends ZuulFilter {

    private static final JsonParser   JSONPARSER = new JsonParser();

    private final GrpcRemoteComponent remoteComponent;

    public GrpcRemoteRouteFilter(GrpcRemoteComponent grpcRemote){
        this.remoteComponent = grpcRemote;
    }

    @Override
    public boolean shouldFilter() {
        ZuulRouteDto route = loadRouteFromCache();
        return (route != null && route.getIsGrpc() != null) ? route.getIsGrpc() : false;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.getBoolean("LimitAccess")) {
            ZuulRouteDto route = loadRouteFromCache();
            Map<String, String> fieldMapping = route.getMappingField();
            if (fieldMapping.isEmpty()) {
                this.doJsonRun();
            } else {
                this.doFieldMappingRun();
            }
        }
        return null;
    }

    private void doJsonRun() {
        ZuulRouteDto route = this.loadRouteFromCache();
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String jsonParam = null;
        Collection<String[]> paramValues = request.getParameterMap().values();
        for (String[] value : paramValues) {
            for (String valueV : value) {
                try {
                    JSONPARSER.parse(valueV);
                    jsonParam = valueV;
                    break;
                } catch (JsonParseException e) {
                    continue;
                }
            }
            if (jsonParam != null) break;
        }
        if (jsonParam == null) {
            ctx.setResponseStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            ctx.setResponseBody("Can not find right param, the param must be json and must be only one");
        } else {
            try {
                String result = remoteComponent.callRemoteService(route.getServiceName(), route.getGroup(),
                                                                  route.getVersion(), route.getMethod(), jsonParam);
                ctx.setResponseStatusCode(HttpServletResponse.SC_OK);
                ctx.setResponseBody(result);
            } catch (Throwable e) {
                ctx.setResponseStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ctx.setResponseBody(e.getMessage());
            }
        }
    }

    private void doFieldMappingRun() {
        ZuulRouteDto route = this.loadRouteFromCache();
        Map<String, String> fieldMap = route.getMappingField();
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();
        Map<String, String> valueMap = Maps.newHashMap();
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            String sourceFieldName = entry.getKey();
            String targetFieldName = entry.getValue();
            String fieldValue = request.getParameter(sourceFieldName);
            valueMap.put(targetFieldName, fieldValue);
        }
        if (valueMap.isEmpty()) {
            ctx.set("error.status_code", 400);
            ctx.set("error.message", "param can not be empty");
        } else {
            try {
                String result = remoteComponent.callRemoteService(route.getServiceName(), route.getGroup(),
                                                                  route.getVersion(), route.getMethod(), valueMap);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().print(result);
            } catch (Throwable e) {
                ctx.set("error.status_code", 500);
                ctx.set("error.message", e.getMessage());
                ctx.set("error.exception", e);
            }
        }
    }

    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 9;
    }

    private ZuulRouteDto loadRouteFromCache() {
        RequestContext context = RequestContext.getCurrentContext();
        String requestPath = context.getRequest().getServletPath();
        Set<ZuulRouteDto> routes = RouterLocalCache.getInstance().getRouters();
        for (ZuulRouteDto route : routes) {
            if (route.getRoutePath().contains(requestPath)) {
                return route;
            }
        }
        return null;
    }

}
