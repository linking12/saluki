/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.filters.route;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

/**
 * @author shimingliu 2017年3月23日 上午10:43:32
 * @version GrpcRemoteApiFilter.java, v 0.0.1 2017年3月23日 上午10:43:32 shimingliu
 */
public class GrpcRemoteApiFilter extends ZuulFilter {

    @Override
    public boolean shouldFilter() {
        RequestContext context = RequestContext.getCurrentContext();
        String requestPath = context.getRequest().getServletPath();
        return false;
    }

    @Override
    public Object run() {
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
