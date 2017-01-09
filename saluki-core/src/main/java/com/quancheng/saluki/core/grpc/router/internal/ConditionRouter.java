/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.router.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.common.GrpcURLUtils;
import com.quancheng.saluki.core.grpc.router.GrpcRouter;

import io.grpc.EquivalentAddressGroup;

/**
 * @author shimingliu 2017年1月9日 下午2:22:04
 * @version ConditionRouter.java, v 0.0.1 2017年1月9日 下午2:22:04 shimingliu
 */
public class ConditionRouter extends GrpcRouter {

    private static final Logger                 log           = LoggerFactory.getLogger(ConditionRouter.class);

    private static final Pattern                ROUTE_PATTERN = Pattern.compile("([&!=,]*)\\s*([^&!=,\\s]+)");

    private static final Map<String, MatchPair> condition     = new HashMap<String, MatchPair>();

    public ConditionRouter(GrpcURL url, String routerMessage){
        super(url, routerMessage);
    }

    @Override
    protected void parseRouter() {
        String rule = super.getRouterMessage();
        int i = rule.indexOf("=>");
        String whenRule = i < 0 ? null : rule.substring(0, i).trim();
        String thenRule = i < 0 ? rule.trim() : rule.substring(i + 2).trim();
    }

    @Override
    public boolean match(EquivalentAddressGroup server) {
        // TODO Auto-generated method stub
        return false;
    }

    private static final class MatchPair {

        final Set<String> matches    = new HashSet<String>();
        final Set<String> mismatches = new HashSet<String>();

        public boolean isMatch(String value, GrpcURL param) {
            for (String match : matches) {
                if (!GrpcURLUtils.isMatchGlobPattern(match, value, param)) {
                    return false;
                }
            }
            for (String mismatch : mismatches) {
                if (GrpcURLUtils.isMatchGlobPattern(mismatch, value, param)) {
                    return false;
                }
            }
            return true;
        }
    }

}
