/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.filters.pre;

import javax.servlet.http.HttpServletRequest;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.quancheng.saluki.gateway.oauth2.limiter.RateLimiter;
import com.quancheng.saluki.gateway.oauth2.support.Oauth2UserStore;

/**
 * @author shimingliu 2017年3月23日 上午10:43:08
 * @version Oauth2Filter.java, v 0.0.1 2017年3月23日 上午10:43:08 shimingliu
 */
public class Oauth2AccessFilter extends ZuulFilter {

    private final Oauth2UserStore oauth2Userstore;

    private final RateLimiter     rateLimiter;

    public Oauth2AccessFilter(Oauth2UserStore userDao, RateLimiter rateLimiter){
        super();
        this.oauth2Userstore = userDao;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String auth = request.getHeader("Authorization");
        String accessToken = auth.split(" ")[1];
        String username = oauth2Userstore.loadUsernameByToken(accessToken);
        if (!rateLimiter.access(username)) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            ctx.setResponseBody("The times of usage is limited");
        }
        return null;
    }

}
