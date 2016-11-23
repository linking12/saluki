package com.quancheng.saluki.monitor.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.saluki.monitor.SalukiService;
import com.quancheng.saluki.monitor.service.ConsulRegistryService;
import com.quancheng.saluki.monitor.service.GenericRpcCallService;
import com.quancheng.saluki.monitor.service.support.model.MethodDefinition;

@RestController
@RequestMapping(value = "service")
public class SalukiServiceController {

    private Logger                log = Logger.getLogger(ApplicationController.class);

    @Autowired
    private ConsulRegistryService registrySerivce;

    @Autowired
    private GenericRpcCallService genericRpcCallService;

    @RequestMapping(value = "/fuzzyapp", method = RequestMethod.GET)
    public List<SalukiService> queryByApp(@RequestParam(value = "search", required = true) String search) {
        log.info("Return all service from registry");
        return registrySerivce.queryPassingServiceByApp(search, Boolean.FALSE);
    }

    @RequestMapping(value = "/fuzzyservice", method = RequestMethod.GET)
    public List<SalukiService> queryByService(@RequestParam(value = "search", required = true) String search) {
        log.info("Return all service from registry");
        return registrySerivce.queryPassingServiceByService(search, Boolean.FALSE);
    }

    @RequestMapping(value = "/accurateapp", method = RequestMethod.GET)
    public List<SalukiService> getByApp(@RequestParam(value = "search", required = true) String search) {
        log.info("Return all service from registry");
        return registrySerivce.queryPassingServiceByApp(search, Boolean.TRUE);
    }

    @RequestMapping(value = "/accurateservice", method = RequestMethod.GET)
    public List<SalukiService> getByService(@RequestParam(value = "search", required = true) String search) {
        log.info("Return all service from registry");
        return registrySerivce.queryPassingServiceByService(search, Boolean.TRUE);
    }

    @RequestMapping(value = "/listAllMethod", method = RequestMethod.GET)
    public List<MethodDefinition> getAllMethod(@RequestParam(value = "service", required = true) String service) throws ClassNotFoundException {
        return genericRpcCallService.getAllMethod(service);
    }

    @RequestMapping(value = "/getMethod", method = RequestMethod.GET)
    public MethodDefinition getMethod(@RequestParam(value = "service", required = true) String service,
                                      @RequestParam(value = "method", required = true) String method) {
        return genericRpcCallService.getMethod(service, method);
    }

}
