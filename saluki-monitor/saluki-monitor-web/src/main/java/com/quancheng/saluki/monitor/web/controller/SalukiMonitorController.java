package com.quancheng.saluki.monitor.web.controller;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "monitor")
public class SalukiMonitorController {

    private Logger     log = Logger.getLogger(ApplicationController.class);

    private HttpClient httpClient;

    @PostConstruct
    public void init() {
        httpClient = HttpClientBuilder.create().build();
    }

    @RequestMapping(value = "data", method = RequestMethod.GET)
    public String data(@RequestParam(value = "ip", required = true) String ip,
                       @RequestParam(value = "port", required = true) String port,
                       @RequestParam(value = "service", required = true) String service) throws Exception {
        log.info("Return all monitor data");
        String monitordataUrl = "http://" + ip + ":" + port + "/salukiMonitor/data?service=" + service;
        HttpGet request = new HttpGet(monitordataUrl);
        request.addHeader("content-type", "application/json");
        request.addHeader("Accept", "application/json");
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            throw e;
        }
    }

    @RequestMapping(value = "system", method = RequestMethod.GET)
    public String system(@RequestParam(value = "ip", required = true) String ip,
                         @RequestParam(value = "port", required = true) String port) throws Exception {
        log.info("Return all monitor data");
        String monitordataUrl = "http://" + ip + ":" + port + "/salukiMonitor/system";
        HttpGet request = new HttpGet(monitordataUrl);
        request.addHeader("content-type", "application/json");
        request.addHeader("Accept", "application/json");
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            String response = EntityUtils.toString(httpResponse.getEntity());
            return response;
        } catch (Exception e) {
            throw e;
        }
    }

}
