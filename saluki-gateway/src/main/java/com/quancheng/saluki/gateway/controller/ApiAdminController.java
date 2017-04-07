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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quancheng.saluki.gateway.grpc.repository.ApiJarRepository;
import com.quancheng.saluki.gateway.grpc.service.ApiJarService;

/**
 * @author shimingliu 2017年4月7日 下午4:53:57
 * @version ApiAdminController.java, v 0.0.1 2017年4月7日 下午4:53:57 shimingliu
 */

@Controller
@RequestMapping("/api.html")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ApiAdminController {

    @Autowired
    private ApiJarService    apiJarService;

    @Autowired
    private ApiJarRepository jarRespository;

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                             MediaType.APPLICATION_XHTML_XML_VALUE })
    public String listAllApiJar(@RequestParam(name = "type", required = false) String editType, Model model,
                                Pageable pageable) {
        if (!StringUtils.isEmpty(editType)) {
            return "api/api";
        }
        model.addAttribute("apis", jarRespository.findAll(pageable));
        return "api/apis";
    }

    @RequestMapping(method = RequestMethod.POST, produces = { MediaType.TEXT_HTML_VALUE,
                                                              MediaType.APPLICATION_XHTML_XML_VALUE })
    public String createApiJar(@RequestParam("jarUrl") String jarUrl, @RequestParam("jarVersion") String jarVersion,
                               RedirectAttributes attributes) {

        apiJarService.saveJar(jarVersion, jarUrl);
        return "redirect:/api.html";
    }
}
