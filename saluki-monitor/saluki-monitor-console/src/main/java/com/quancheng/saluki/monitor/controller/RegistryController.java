package com.quancheng.saluki.monitor.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.saluki.domain.GrpcService;
import com.quancheng.saluki.monitor.service.ConsulRegistryService;

@RestController
@RequestMapping(value = "/api/service")
public class RegistryController {

    private static final Logger   log = Logger.getLogger(RegistryController.class);

    @Autowired
    private ConsulRegistryService registrySerivce;

    @RequestMapping(value = "/fuzzyapp", method = RequestMethod.GET)
    public List<GrpcService> queryByApp(@RequestParam(value = "search", required = true) String search) {
        log.info("Return all service from registry");
        return registrySerivce.queryPassingServiceByApp(search, Boolean.FALSE);
    }

    @RequestMapping(value = "/fuzzyservice", method = RequestMethod.GET)
    public List<GrpcService> queryByService(@RequestParam(value = "search", required = true) String search) {
        log.info("Return all service from registry");
        return registrySerivce.queryPassingServiceByService(search, Boolean.FALSE);
    }

    @RequestMapping(value = "/accurateapp", method = RequestMethod.GET)
    public List<GrpcService> getByApp(@RequestParam(value = "search", required = true) String search) {
        log.info("Return all service from registry");
        return registrySerivce.queryPassingServiceByApp(search, Boolean.TRUE);
    }

    @RequestMapping(value = "/accurateservice", method = RequestMethod.GET)
    public List<GrpcService> getByService(@RequestParam(value = "search", required = true) String search) {
        log.info("Return all service from registry");
        return registrySerivce.queryPassingServiceByService(search, Boolean.TRUE);
    }

}
