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

/**
 * @author shimingliu 2016年12月29日 下午7:52:14
 * @version GrpcRouter.java, v 0.0.1 2016年12月29日 下午7:52:14 shimingliu
 */
public abstract class GrpcRouter {

    private final String rule;

    private GrpcURL      refUrl;

    public GrpcRouter(String rule){
        this.rule = rule;
        parseRouter();
    }

    public String getRule() {
        return rule;
    }

    public GrpcURL getRefUrl() {
        return refUrl;
    }

    public void setRefUrl(GrpcURL refUrl) {
        this.refUrl = refUrl;
    }

    protected abstract void parseRouter();

    public abstract boolean match(List<GrpcURL> providerUrl);
}
