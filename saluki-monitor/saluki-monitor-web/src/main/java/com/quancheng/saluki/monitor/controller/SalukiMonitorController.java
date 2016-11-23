package com.quancheng.saluki.monitor.controller;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
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

import com.google.common.collect.Maps;

@RestController
@RequestMapping(value = "monitor")
public class SalukiMonitorController {

    private Logger     log = Logger.getLogger(SalukiMonitorController.class);

    private HttpClient httpClient;

    @PostConstruct
    public void init() {
        httpClient = HttpClientBuilder.create().build();
    }

    @RequestMapping(value = "data", method = RequestMethod.GET)
    public Map<String, String> data(@RequestParam(value = "ipPorts", required = true) String ipPorts,
                                    @RequestParam(value = "service", required = true) String service,
                                    @RequestParam(value = "type", required = true) String type) throws Exception {
        log.info("Return statistics monitor data");
        Map<String, String> datas = Maps.newHashMap();
        for (String ipPort : StringUtils.split(ipPorts, ",")) {
            String monitordataUrl = "http://" + ipPort + "/salukiMonitor/data?service=" + service + "&type=" + type;
            HttpGet request = new HttpGet(monitordataUrl);
            request.addHeader("content-type", "application/json");
            request.addHeader("Accept", "application/json");
            try {
                HttpResponse httpResponse = httpClient.execute(request);
                String monitorData = EntityUtils.toString(httpResponse.getEntity());
                datas.put(ipPort, monitorData);
            } catch (Exception e) {
                throw e;
            }
        }
        return datas;
    }

    @RequestMapping(value = "statistics", method = RequestMethod.GET)
    public Map<String, String> statistics(@RequestParam(value = "ipPorts", required = true) String ipPorts,
                                          @RequestParam(value = "service", required = true) String service,
                                          @RequestParam(value = "type", required = true) String type) throws Exception {
        log.info("Return statistics monitor data");
        Map<String, String> statistics = Maps.newHashMap();
        for (String ipPort : StringUtils.split(ipPorts, ",")) {
            String monitordataUrl = "http://" + ipPort + "/salukiMonitor/statistics?service=" + service + "&type="
                                    + type;
            HttpGet request = new HttpGet(monitordataUrl);
            request.addHeader("content-type", "application/json");
            request.addHeader("Accept", "application/json");
            try {
                HttpResponse httpResponse = httpClient.execute(request);
                String monitorData = EntityUtils.toString(httpResponse.getEntity());
                statistics.put(ipPort, monitorData);
            } catch (Exception e) {
                throw e;
            }
        }
        return statistics;
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
