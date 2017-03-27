/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.quancheng.saluki.gateway.grpc.GrpcRemoteComponent;

/**
 * @author shimingliu 2017年3月24日 下午3:01:03
 * @version WelcomeController.java, v 0.0.1 2017年3月24日 下午3:01:03 shimingliu
 */
@Controller
@RequestMapping("/web/welcome")
public class WelcomeController {

    @Autowired
    private GrpcRemoteComponent grpcRemoteComponet;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public String welcome() {
        return "Hello,This is saluki gateway";
    }

    @RequestMapping(value = "/testCall", method = RequestMethod.GET)
    @ResponseBody
    public String callRemote(@RequestParam(value = "service", required = true) String service,
                             @RequestParam(value = "method", required = true) String method,
                             @RequestParam(value = "group", required = true) String group,
                             @RequestParam(value = "version", required = true) String version,
                             @RequestParam(value = "param", required = true) String param) throws Throwable {
        Object obj = grpcRemoteComponet.callRemoteService(service, group, version, method, param);
        return new Gson().toJson(obj);
    }

}
