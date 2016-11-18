package com.quancheng.saluki.monitor.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.saluki.monitor.domain.SalukiApplication;
import com.quancheng.saluki.monitor.service.ConsulRegistryService;

@RestController
@RequestMapping(value = "application")
public class ApplicationController {

    private Logger                log = Logger.getLogger(ApplicationController.class);

    @Autowired
    private ConsulRegistryService registrySerivce;

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public List<SalukiApplication> listAllApps() {
        log.info("Return all application from registry");
        return registrySerivce.getAllApplication();
    }

}
