package com.quancheng.saluki.monitor.web.controller;

import java.util.List;
import java.util.Map;

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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quancheng.saluki.monitor.domain.SalukiInvokeStatistics;

@RestController
@RequestMapping(value = "monitor")
public class SalukiMonitorDataController {

    private Logger     log  = Logger.getLogger(ApplicationController.class);

    private HttpClient httpClient;

    private final Gson gson = new Gson();

    @PostConstruct
    public void init() {
        httpClient = HttpClientBuilder.create().build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<SalukiInvokeStatistics> data(@RequestParam(value = "ip", required = true) String ip,
                                             @RequestParam(value = "port", required = true) String port,
                                             @RequestParam(value = "service", required = true) String service) throws Exception {
        log.info("Return all monitor data");
        String monitordataUrl = "http://" + ip + ":" + port + "/salukiMonitor/data?service=" + service;
        HttpGet request = new HttpGet(monitordataUrl);
        request.addHeader("content-type", "application/json");
        request.addHeader("Accept", "application/json");
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            String response = EntityUtils.toString(httpResponse.getEntity());

            List<SalukiInvokeStatistics> statistics = gson.fromJson(response,
                                                                    new TypeToken<List<SalukiInvokeStatistics>>() {
                                                                    }.getType());
            return statistics;
        } catch (Exception e) {
            throw e;
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> system(@RequestParam(value = "ip", required = true) String ip,
                                      @RequestParam(value = "port", required = true) String port) throws Exception {
        log.info("Return all monitor data");
        String monitordataUrl = "http://" + ip + ":" + port + "/salukiMonitor/system";
        HttpGet request = new HttpGet(monitordataUrl);
        request.addHeader("content-type", "application/json");
        request.addHeader("Accept", "application/json");
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            String response = EntityUtils.toString(httpResponse.getEntity());
            Map<String, Object> system = gson.fromJson(response, new TypeToken<Map<String, Object>>() {
            }.getType());
            return system;
        } catch (Exception e) {
            throw e;
        }
    }

}
