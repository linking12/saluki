/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.grpc.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.gateway.grpc.entity.ApiJarEntity;
import com.quancheng.saluki.gateway.grpc.repository.ApiJarRepository;

import sun.misc.BASE64Encoder;

/**
 * @author shimingliu 2017年3月27日 下午6:55:37
 * @version ApiJarService.java, v 0.0.1 2017年3月27日 下午6:55:37 shimingliu
 */
@Service
@SuppressWarnings("restriction")
public class ApiJarService {

    private static final Logger            logger          = LoggerFactory.getLogger(ApiJarService.class);

    private static final String            AUTHOR          = new BASE64Encoder().encode(("liushiming:Hello899").getBytes());

    private final ScheduledExecutorService refreshExecutor = Executors.newScheduledThreadPool(1,
                                                                                              new NamedThreadFactory("refreshZuulRoute",
                                                                                                                     true));

    private String                         API_DIR_PATH    = System.getProperty("user.home") + File.separator
                                                             + "saluki";

    private String                         API_JAR_PATH;

    @Autowired
    private ApiJarRepository               jarRespository;

    @PostConstruct
    public void init() {
        API_JAR_PATH = API_DIR_PATH + File.separator + "api.jar";
        refreshExecutor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    refresh();
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

    private void refresh() throws IOException {
        List<ApiJarEntity> entitys = jarRespository.findLatestJar(new PageRequest(0, 1));
        if (entitys.size() > 0) {
            ApiJarEntity entity = entitys.get(0);
            Date jarCreateTime = entity.getCreateTime();
            Date now = new Date();
            long times = (now.getTime() - jarCreateTime.getTime()) / (60 * 60);
            if (times > 0 && times <= 30) {
                this.downloadApiJar(entity.getJarUrl());
            }
        }
    }

    public Boolean saveJar(String jarVersion, String jarUrl) {
        ApiJarEntity apiJar = new ApiJarEntity();
        apiJar.setJarUrl(jarUrl);
        apiJar.setJarVersion(jarVersion);
        apiJar.setCreateTime(new Date());
        try {
            downloadApiJar(jarUrl);
            jarRespository.save(apiJar);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void downloadApiJar(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3 * 1000);
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        conn.setRequestProperty("Authorization", "Basic " + AUTHOR);
        InputStream inputStream = conn.getInputStream();
        byte[] getData = readInputStream(inputStream);
        File saveDir = new File(API_DIR_PATH);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        File file = new File(API_JAR_PATH);
        file.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if (fos != null) {
            fos.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
        logger.info(url + " download success");
    }

    private byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

}
