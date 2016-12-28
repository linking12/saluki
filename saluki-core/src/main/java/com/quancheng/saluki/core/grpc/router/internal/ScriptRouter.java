/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
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
 * @author shimingliu 2016年12月28日 下午8:40:59
 * @version ScriptRouter.java, v 0.0.1 2016年12月28日 下午8:40:59 shimingliu
 */
public class ScriptRouter implements GrpcRouter {

    @Override
    public List<? extends List<ResolvedServerInfo>> router(GrpcURL url, List<ResolvedServerInfo> servers) {
        // TODO Auto-generated method stub
        return null;
    }

}
