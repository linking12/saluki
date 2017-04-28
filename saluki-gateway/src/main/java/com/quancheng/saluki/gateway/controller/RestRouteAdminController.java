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
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.env.Environment;
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
 * @author shimingliu 2017年4月7日 上午10:31:11
 * @version RouteAdminController.java, v 0.0.1 2017年4月7日 上午10:31:11 shimingliu
 */
@Controller
@RequestMapping("/restRoute.html")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class RestRouteAdminController implements ApplicationEventPublisherAware {

    @Autowired
    private ZuulRouteRepository       zuulRouteRepository;

    @Autowired
    private Environment               environment;

    private ApplicationEventPublisher publisher;

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
                    model.addAttribute("routeUrl", zuulRouteEntity.getUrl());
                    model.addAttribute("stripPrefix", zuulRouteEntity.getStrip_prefix());
                    model.addAttribute("retryAble", zuulRouteEntity.getRetryable());
                    model.addAttribute("sensitiveHeaders", zuulRouteEntity.getSensitiveHeaders());
                    return null;
                });
            }
            return "route/restroute";
        }
        model.addAttribute("routeList", zuulRouteRepository.findAllRest(pageable));
        return "route/restroutes";
    }

    @RequestMapping(path = "/_create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                                         MediaType.APPLICATION_XHTML_XML_VALUE })
    public String create(@RequestParam(name = "routeId", required = true) String routeId,
                         @RequestParam(name = "routePath", required = true) String routePath,
                         @RequestParam(name = "routeUrl", required = true) String routeUrl,
                         @RequestParam(name = "stripPrefix", defaultValue = "false") Boolean stripPrefix,
                         @RequestParam(name = "retryAble", defaultValue = "false") Boolean retryAble,
                         @RequestParam(name = "sensitiveHeaders", defaultValue = "") String sensitiveHeaders,
                         RedirectAttributes attributes) {
        if (zuulRouteRepository.findOneByRouteId(routeId).isPresent()) {
            addErrorMessage(attributes, routeId + "已经存在 ");
            resetRequestParams(routeId, routePath, routeUrl, stripPrefix, retryAble, sensitiveHeaders, attributes);
            return "redirect:/restRoute.html?type=add";
        }
        ZuulRouteEntity entityRest = ZuulRouteEntity.builder()//
                                                    .zuul_route_id(routeId)//
                                                    .path(routePath)//
                                                    .strip_prefix(stripPrefix)//
                                                    .retryable(retryAble)//
                                                    .url(routeUrl)//
                                                    .sensitiveHeaders(sensitiveHeaders)//
                                                    .build();
        zuulRouteRepository.save(entityRest);
        publisher.publishEvent(new InstanceRegisteredEvent<>(this, this.environment));
        return "redirect:/restRoute.html";
    }

    @RequestMapping(path = "/_update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                                         MediaType.APPLICATION_XHTML_XML_VALUE })
    public String update(@RequestParam(name = "routeId", required = true) String routeId,
                         @RequestParam(name = "routePath", required = true) String routePath,
                         @RequestParam(name = "routeUrl", required = true) String routeUrl,
                         @RequestParam(name = "stripPrefix", defaultValue = "false") Boolean stripPrefix,
                         @RequestParam(name = "retryAble", defaultValue = "false") Boolean retryAble,
                         @RequestParam(name = "sensitiveHeaders", defaultValue = "") String sensitiveHeaders,
                         RedirectAttributes attributes) {

        zuulRouteRepository.findOneByRouteId(routeId).map(zuulRouteEntity -> {
            zuulRouteEntity.setZuul_route_id(routeId);
            zuulRouteEntity.setPath(routePath);
            zuulRouteEntity.setUrl(routeUrl);
            zuulRouteEntity.setStrip_prefix(stripPrefix);
            zuulRouteEntity.setRetryable(retryAble);
            zuulRouteEntity.setSensitiveHeaders(sensitiveHeaders);
            return zuulRouteRepository.save(zuulRouteEntity);
        }).orElseGet(() -> {
            addErrorMessage(attributes, "routeId" + routeId + " 不存在。");
            return null;
        });
        publisher.publishEvent(new InstanceRegisteredEvent<>(this, this.environment));
        return "redirect:/restRoute.html";
    }

    @RequestMapping(path = "/_remove/{routeId}", method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                          MediaType.APPLICATION_XHTML_XML_VALUE })
    public String deleteRoute(@PathVariable("routeId") String routeId, RedirectAttributes attributes) {

        zuulRouteRepository.findOneByRouteId(routeId).map(zuulRouteEntity -> {
            zuulRouteRepository.delete(zuulRouteEntity);
            addSuccessMessage(attributes, "路由 " + routeId + " 已删除。");
            publisher.publishEvent(new InstanceRegisteredEvent<>(this, this.environment));
            return zuulRouteEntity;
        }).orElseGet(() -> {
            addWarningMessage(attributes, "没有找到 " + routeId + " 路由。");
            return null;
        });
        return "redirect:/restRoute.html";
    }

    private void resetRequestParams(String routeId, String routePath, String routeUrl, Boolean stripPrefix,
                                    Boolean retryAble, String sensitiveHeaders, RedirectAttributes attributes) {

        attributes.addFlashAttribute("routeId", routeId);
        attributes.addFlashAttribute("routePath", routePath);
        attributes.addFlashAttribute("routeUrl", routeUrl);
        attributes.addFlashAttribute("stripPrefix", stripPrefix);
        attributes.addFlashAttribute("retryAble", retryAble);
        attributes.addFlashAttribute("sensitiveHeaders", sensitiveHeaders);

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
