/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.controller;

import static com.quancheng.saluki.gateway.controller.RedirectMessageHelper.addErrorMessage;
import static com.quancheng.saluki.gateway.controller.RedirectMessageHelper.addSuccessMessage;
import static com.quancheng.saluki.gateway.controller.RedirectMessageHelper.addWarningMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quancheng.saluki.gateway.zuul.entity.ZuulRouteEntity;
import com.quancheng.saluki.gateway.zuul.repository.ZuulRouteRepository;

/**
 * @author shimingliu 2017年4月7日 上午11:51:14
 * @version GrpcRouteAdminController.java, v 0.0.1 2017年4月7日 上午11:51:14 shimingliu
 */
@Controller
@RequestMapping("/grpcRoute.html")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class GrpcRouteAdminController {

    @Autowired
    private ZuulRouteRepository zuulRouteRepository;

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                             MediaType.APPLICATION_XHTML_XML_VALUE })
    public String listAll(@RequestParam(name = "type", required = false) String editType,
                          @RequestParam(name = "edit", required = false) String editRestRoute, Model model,
                          Pageable pageable) {

        if (!StringUtils.isEmpty(editType)) {
            if (!StringUtils.isEmpty(editRestRoute)) {
                zuulRouteRepository.findOneByRouteId(editRestRoute).map(zuulRouteEntity -> {

                    model.addAttribute("routeId", zuulRouteEntity.getZuul_route_id());
                    model.addAttribute("routePath", zuulRouteEntity.getPath());
                    model.addAttribute("isGrpc", true);
                    model.addAttribute("serviceName", zuulRouteEntity.getService_name());
                    model.addAttribute("group", zuulRouteEntity.getGroup());
                    model.addAttribute("version", zuulRouteEntity.getVersion());
                    model.addAttribute("method", zuulRouteEntity.getMethod());
                    return null;
                });
            }
            return "route/grpcroute";
        }
        model.addAttribute("routeList", zuulRouteRepository.findAllGrpc(pageable));
        return "route/grpcroutes";
    }

    @RequestMapping(path = "/_create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                                         MediaType.APPLICATION_XHTML_XML_VALUE })
    public String create(@RequestParam(name = "routeId", required = true) String routeId,
                         @RequestParam(name = "routePath", required = true) String routePath,
                         @RequestParam(name = "isGrpc", required = true) Boolean isGrpc,
                         @RequestParam(name = "serviceName", required = true) String serviceName,
                         @RequestParam(name = "group", required = true) String group,
                         @RequestParam(name = "version", required = true) String version,
                         @RequestParam(name = "method", required = true) String method, RedirectAttributes attributes) {
        if (zuulRouteRepository.findOneByRouteId(routeId).isPresent()) {
            addErrorMessage(attributes, routeId + "已经存在 ");
            resetRequestParams(routeId, routePath, isGrpc, serviceName, group, version, method, attributes);
            return "redirect:/grpcRoute.html?type=add";
        }
        ZuulRouteEntity entityGrpc = ZuulRouteEntity.builder()//
                                                    .zuul_route_id(routeId)//
                                                    .path(routePath)//
                                                    .is_grpc(true)//
                                                    .service_name(serviceName)//
                                                    .group(group)//
                                                    .version(version)//
                                                    .method(method)//
                                                    .build();
        zuulRouteRepository.save(entityGrpc);
        return "redirect:/grpcRoute.html";
    }

    @RequestMapping(path = "/_update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                                         MediaType.APPLICATION_XHTML_XML_VALUE })
    public String update(@RequestParam(name = "routeId", required = true) String routeId,
                         @RequestParam(name = "routePath", required = true) String routePath,
                         @RequestParam(name = "isGrpc", required = true) Boolean isGrpc,
                         @RequestParam(name = "serviceName", required = true) String serviceName,
                         @RequestParam(name = "group", required = true) String group,
                         @RequestParam(name = "version", required = true) String version,
                         @RequestParam(name = "method", required = true) String method, RedirectAttributes attributes) {

        zuulRouteRepository.findOneByRouteId(routeId).map(zuulRouteEntity -> {
            zuulRouteEntity.setZuul_route_id(routeId);
            zuulRouteEntity.setPath(routePath);
            zuulRouteEntity.setIs_grpc(true);
            zuulRouteEntity.setService_name(serviceName);
            zuulRouteEntity.setGroup(group);
            zuulRouteEntity.setVersion(version);
            zuulRouteEntity.setMethod(method);
            return zuulRouteRepository.save(zuulRouteEntity);
        }).orElseGet(() -> {
            addErrorMessage(attributes, "routeId" + routeId + " 不存在。");
            return null;
        });
        return "redirect:/grpcRoute.html";
    }

    @RequestMapping(path = "/_remove/{routeId}", method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                          MediaType.APPLICATION_XHTML_XML_VALUE })
    public String deleteRoute(@PathVariable("routeId") String routeId, RedirectAttributes attributes) {

        zuulRouteRepository.findOneByRouteId(routeId).map(zuulRouteEntity -> {
            zuulRouteRepository.delete(zuulRouteEntity);
            addSuccessMessage(attributes, "路由 " + routeId + " 已删除。");
            return zuulRouteEntity;
        }).orElseGet(() -> {
            addWarningMessage(attributes, "没有找到 " + routeId + " 路由。");
            return null;
        });
        return "redirect:/grpcRoute.html";
    }

    private void resetRequestParams(String routeId, String routePath, Boolean isGrpc, String serviceName, String group,
                                    String version, String method, RedirectAttributes attributes) {

        attributes.addFlashAttribute("routeId", routeId);
        attributes.addFlashAttribute("routePath", routePath);
        attributes.addFlashAttribute("isGrpc", isGrpc);
        attributes.addFlashAttribute("serviceName", serviceName);
        attributes.addFlashAttribute("group", group);
        attributes.addFlashAttribute("version", version);
        attributes.addFlashAttribute("method", method);

    }
}
