/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.quancheng.saluki.gateway.zuul.repository.ZuulRouteRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author shimingliu 2017年4月7日 上午10:31:11
 * @version RouteAdminController.java, v 0.0.1 2017年4月7日 上午10:31:11 shimingliu
 */
@Slf4j
@Controller
@RequestMapping("/clientDetails.html")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class RestRouteAdminController {

    @Autowired
    private ZuulRouteRepository zuulRouteRepository;

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                             MediaType.APPLICATION_XHTML_XML_VALUE })
    public String listAll(@RequestParam(name = "type", required = false) String editType,
                          @RequestParam(name = "edit", required = false) String editClientDetails, Model model,
                          Pageable pageable) {

        if (!StringUtils.isEmpty(editType)) {
            if (!StringUtils.isEmpty(editClientDetails)) {
            }

        }
        model.addAttribute("routeList", zuulRouteRepository.findAllRest(pageable));
        return "route/restroutes";
    }

}
