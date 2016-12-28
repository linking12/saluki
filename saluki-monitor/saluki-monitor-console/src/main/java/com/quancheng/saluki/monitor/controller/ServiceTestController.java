package com.quancheng.saluki.monitor.controller;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.quancheng.saluki.domain.GrpcServiceTestModel;
import com.quancheng.saluki.boot.jaket.model.GenericInvokeMetadata;
import com.quancheng.saluki.boot.jaket.model.MethodDefinition;

@RestController
@RequestMapping("/api/serviceMeasure")
public class ServiceTestController {

    private HttpClient httpClient;

    private Gson       gson;

    @PostConstruct
    public void init() {
        httpClient = HttpClientBuilder.create().build();
        gson = new GsonBuilder().create();
    }

    @RequestMapping(value = "getAllMethod", method = RequestMethod.GET)
    public List<MethodDefinition> getAllMethod(@RequestParam(value = "ipPort", required = true) String ipPort,
                                               @RequestParam(value = "service", required = true) String service) throws Exception {

        String methdUrl = "http://" + ipPort + "/service/getAllMethod?service=" + service;
        HttpGet request = new HttpGet(methdUrl);
        request.addHeader("content-type", "application/json");
        request.addHeader("Accept", "application/json");
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String minitorJson = EntityUtils.toString(httpResponse.getEntity());
                List<MethodDefinition> allMethods = gson.fromJson(minitorJson, new TypeToken<List<MethodDefinition>>() {
                }.getType());
                return allMethods;
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    @RequestMapping(value = "getMethod", method = RequestMethod.GET)
    public GenericInvokeMetadata getMethod(@RequestParam(value = "ipPort", required = true) String ipPort,
                                           @RequestParam(value = "service", required = true) String service,
                                           @RequestParam(value = "method", required = true) String method) throws Exception {
        String methdUrl = "http://" + ipPort + "/service/getMethod?service=" + service + "&method=" + method;
        HttpGet request = new HttpGet(methdUrl);
        request.addHeader("content-type", "application/json");
        request.addHeader("Accept", "application/json");
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String minitorJson = EntityUtils.toString(httpResponse.getEntity());
                GenericInvokeMetadata methodmeta = gson.fromJson(minitorJson, new TypeToken<GenericInvokeMetadata>() {
                }.getType());
                return methodmeta;
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    @RequestMapping(value = "testService", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object testService(@RequestParam(value = "ipPort", required = true) String ipPort,
                              @RequestBody GrpcServiceTestModel model) throws Exception {
        String serviceUrl = "http://" + ipPort + "/service/test";
        HttpPost request = new HttpPost(serviceUrl);
        request.addHeader("content-type", "application/json");
        request.addHeader("Accept", "application/json");
        try {
            StringEntity entity = new StringEntity(gson.toJson(model), "utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            request.setEntity(entity);
            HttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String minitorJson = EntityUtils.toString(httpResponse.getEntity());
                Object response = gson.fromJson(minitorJson, new TypeToken<Object>() {
                }.getType());
                return response;
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

}
