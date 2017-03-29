/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author shimingliu 2017年2月13日 下午2:00:52
 * @version RootController.java, v 0.0.1 2017年2月13日 下午2:00:52 shimingliu
 */
@Controller
@RequestMapping("web")
public class IndexController {

    @RequestMapping(value = "health", method = RequestMethod.GET)
    @ResponseBody
    public String welcome() {
        return "Hello,This is saluki gateway";
    }

    @RequestMapping(value = "index.html", method = RequestMethod.GET)
    public ModelAndView index() {
        return new ModelAndView("index");
    }
}
