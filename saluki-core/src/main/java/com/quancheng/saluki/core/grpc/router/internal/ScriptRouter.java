/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.router.internal;

import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.router.GrpcRouter;

/**
 * @author shimingliu 2017年1月9日 下午2:24:56
 * @version ScriptRouter.java, v 0.0.1 2017年1月9日 下午2:24:56 shimingliu
 */
public class ScriptRouter extends GrpcRouter {

    private static final Logger log = LoggerFactory.getLogger(ScriptRouter.class);

    private ScriptEngine        engine;

    public ScriptRouter(String type, String rule){
        super(rule);
        engine = new ScriptEngineManager().getEngineByName(type);
        if (engine == null && StringUtils.equals(type, "javascript")) {
            engine = new ScriptEngineManager().getEngineByName("js");
        }
        if (engine == null) {
            throw new IllegalStateException("Unsupported route rule type: " + type + ", rule: " + rule);
        }
    }

    @Override
    protected void parseRouter() {
        // do nothing
    }

    @Override
    public boolean match(List<GrpcURL> providerUrls) {
        String rule = super.getRule();
        try {
            engine.eval(super.getRule());
            Invocable invocable = (Invocable) engine;
            GrpcURL refUrl = super.getRefUrl();
            Object arg = new Gson().fromJson(refUrl.getParameterAndDecoded(Constants.ARG_KEY), Object.class);
            Object obj = invocable.invokeFunction("route", refUrl.removeParameter(Constants.ARG_KEY), providerUrls,
                                                  arg);
            if (obj instanceof Boolean) {
                return (Boolean) obj;
            } else {
                return true;
            }
        } catch (ScriptException | NoSuchMethodException e) {
            log.error("route error , rule has been ignored. rule: " + rule + ", url: " + providerUrls, e);
            return true;
        }
    }

}
