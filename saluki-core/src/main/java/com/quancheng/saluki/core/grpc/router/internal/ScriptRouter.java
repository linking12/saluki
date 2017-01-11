/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.router.internal;

import java.util.List;

import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.router.GrpcRouter;

import io.grpc.ResolvedServerInfo;

/**
 * @author shimingliu 2017年1月9日 下午2:24:56
 * @version ScriptRouter.java, v 0.0.1 2017年1月9日 下午2:24:56 shimingliu
 */
public class ScriptRouter extends GrpcRouter {

    public ScriptRouter(GrpcURL url, String routerMessage){
        super(url, routerMessage);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void parseRouter() {

    }

    @Override
    public List<? extends List<ResolvedServerInfo>> router(List<? extends List<ResolvedServerInfo>> servers) {
        // TODO Auto-generated method stub
        return null;
    }

}
