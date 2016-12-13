package com.quancheng.saluki.monitor.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.saluki.monitor.SalukiAppDependcy;
import com.quancheng.saluki.monitor.SalukiApplication;
import com.quancheng.saluki.monitor.service.ConsulRegistryService;
import com.quancheng.saluki.monitor.service.SalukiAppDependcyService;

@RestController
@RequestMapping(value = "/api/application")
public class ApplicationController {

    private Logger                   log = Logger.getLogger(ApplicationController.class);

    @Autowired
    private ConsulRegistryService    registrySerivce;

    @Autowired
    private SalukiAppDependcyService appDependcyService;

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public List<SalukiApplication> listAllApps() {
        log.info("Return all application from registry");
        return registrySerivce.getAllApplication();
    }

    @RequestMapping(value = "dependcy", method = RequestMethod.GET)
    public List<SalukiAppDependcy> listDependcyApps() {
        return appDependcyService.queryApplicationDependcy();
    }

}
