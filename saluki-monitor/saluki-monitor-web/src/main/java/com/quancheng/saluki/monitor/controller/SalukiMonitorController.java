package com.quancheng.saluki.monitor.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quancheng.saluki.monitor.SalukiInvoke;
import com.quancheng.saluki.monitor.service.SalukiMonitorDataService;

@RestController
@RequestMapping(value = "monitor")
public class SalukiMonitorController {

    private Logger                   log = Logger.getLogger(SalukiMonitorController.class);

    @Autowired
    private SalukiMonitorDataService minitorDataService;

    private HttpClient               httpClient;

    private Gson                     gson;

    @PostConstruct
    public void init() {
        httpClient = HttpClientBuilder.create().build();
        gson = new Gson();
    }

    @RequestMapping(value = "statistics", method = RequestMethod.GET)
    public Map<String, List<SalukiInvoke>> statistics(@RequestParam(value = "ips", required = true) String ips,
                                                      @RequestParam(value = "service", required = true) String service,
                                                      @RequestParam(value = "type", required = true) String type,
                                                      @RequestParam(value = "datatype", required = true) String dataType) throws Exception {
        log.info("Return statistics monitor data");
        String[] ipArray = StringUtils.split(",");
        List<String> ipList = Arrays.asList(ipArray);
        return minitorDataService.queryDataByMachines(service, type, ipList);
    }

    @RequestMapping(value = "sumstatistics", method = RequestMethod.GET)
    public List<SalukiInvoke> statistics(@RequestParam(value = "service", required = true) String service,
                                         @RequestParam(value = "type", required = true) String type,
                                         @RequestParam(value = "datatype", required = true) String dataType) throws Exception {
        log.info("Return statistics monitor data");
        return minitorDataService.querySumDataByService(service, type);
    }

    @RequestMapping(value = "system", method = RequestMethod.GET)
    public Map<String, Object> system(@RequestParam(value = "ipPort", required = true) String ipPort) throws Exception {
        log.info("Return all monitor data");
        String monitordataUrl = "http://" + ipPort + "/salukiMonitor/system";
        HttpGet request = new HttpGet(monitordataUrl);
        request.addHeader("content-type", "application/json");
        request.addHeader("Accept", "application/json");
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            String minitorJson = EntityUtils.toString(httpResponse.getEntity());
            Map<String, Object> system = gson.fromJson(minitorJson, new TypeToken<Map<String, Object>>() {
            }.getType());
            return system;
        } catch (Exception e) {
            throw e;
        }
    }

}
