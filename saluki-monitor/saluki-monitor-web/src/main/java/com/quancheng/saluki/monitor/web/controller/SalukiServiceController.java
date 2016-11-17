package com.quancheng.saluki.monitor.web.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.saluki.monitor.domain.SalukiService;
import com.quancheng.saluki.monitor.web.service.ConsulRegistryService;

@RestController
@RequestMapping(value = "service")
public class SalukiServiceController {

    private Logger                log = Logger.getLogger(ApplicationController.class);

    @Autowired
    private ConsulRegistryService registrySerivce;

    @RequestMapping(value = "/fuzzyapp/{search}", method = RequestMethod.GET)
    public List<SalukiService> queryByApp(@PathVariable("search") String search) {
        log.info("Return all service from registry");
        return registrySerivce.queryPassingServiceByApp(search, Boolean.FALSE);
    }

    @RequestMapping(value = "/fuzzyservice/{search}", method = RequestMethod.GET)
    public List<SalukiService> queryByService(@PathVariable("search") String search) {
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
}
