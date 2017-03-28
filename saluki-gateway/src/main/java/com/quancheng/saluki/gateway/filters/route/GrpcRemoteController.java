/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.filters.route;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.web.ZuulController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.netflix.zuul.context.RequestContext;
import com.quancheng.saluki.gateway.grpc.GrpcRemoteComponent;
import com.quancheng.saluki.gateway.storage.RouterLocalCache;
import com.quancheng.saluki.gateway.storage.support.ZuulRouteEntity;

/**
 * @author shimingliu 2017年3月23日 上午10:43:32
 * @version GrpcRemoteApiFilter.java, v 0.0.1 2017年3月23日 上午10:43:32 shimingliu
 */
public class GrpcRemoteController extends ZuulController {

    private static final Logger     logger     = LoggerFactory.getLogger(GrpcRemoteController.class);

    private static final JsonParser JSONPARSER = new JsonParser();

    private GrpcRemoteComponent     grpcRemote;

    public GrpcRemoteController(GrpcRemoteComponent grpcRemote){
        super();
        this.grpcRemote = grpcRemote;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            ZuulRouteEntity route = findRoute(request);
            if (route.getIs_grpc()) {
                String service = route.getService_name();
                String group = route.getGroup();
                String version = route.getVersion();
                String method = route.getMethod();
                Map<?, ?> params = request.getParameterMap();
                String grpcRequestJson = findJsonFromParams(params);
                if (grpcRequestJson == null) {
                    response.setStatus(400);
                    return JsonView.Render("param is wrong format,must be json format", response);
                } else {
                    try {
                        Object grpcResponse = grpcRemote.callRemoteService(service, group, version, method,
                                                                           grpcRequestJson);
                        return JsonView.Render(grpcResponse, response);
                    } catch (Throwable e) {
                        response.setStatus(500);
                        return JsonView.Render(e.getMessage(), response);
                    }
                }
            } else {
                return super.handleRequest(request, response);
            }
        } finally {
            RequestContext.getCurrentContext().unset();
        }
    }

    private ZuulRouteEntity findRoute(HttpServletRequest request) {
        String requestPath = request.getServletPath();
        List<ZuulRouteEntity> routes = RouterLocalCache.getInstance().getRouters();
        for (ZuulRouteEntity route : routes) {
            if (route.getPath().contains(requestPath)) {
                return route;
            }
        }
        return null;
    }

    private String findJsonFromParams(Map<?, ?> params) {
        Collection<?> paramValues = params.values();
        for (Object value : paramValues) {
            try {
                if (value instanceof String) {
                    String jsonValue = (String) value;
                    JSONPARSER.parse(jsonValue);
                    return jsonValue;
                } else {
                    continue;
                }
            } catch (JsonParseException e) {
                continue;
            }
        }
        return null;
    }

    private static class JsonView {

        private static ModelAndView Render(Object model, HttpServletResponse response) {
            GsonHttpMessageConverter jsonConverter = new GsonHttpMessageConverter();
            MediaType jsonMimeType = MediaType.APPLICATION_JSON;
            try {
                ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
                httpResponse.setStatusCode(HttpStatus.valueOf(response.getStatus()));
                jsonConverter.write(model, jsonMimeType, httpResponse);
            } catch (HttpMessageNotWritableException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return null;
        }
    }

}
