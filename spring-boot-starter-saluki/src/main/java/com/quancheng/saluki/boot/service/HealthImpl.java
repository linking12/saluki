/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.boot.service;

import org.springframework.beans.BeansException;
import org.springframework.context.support.AbstractApplicationContext;

import com.quancheng.saluki.boot.SalukiService;
import com.quancheng.saluki.core.utils.ReflectUtils;
import com.quancheng.saluki.service.Health;
import com.quancheng.saluki.service.serviceparam.HealthCheckRequest;
import com.quancheng.saluki.service.serviceparam.HealthCheckResponse;

/**
 * @author shimingliu 2016年12月29日 下午2:35:00
 * @version EchoServiceImpl.java, v 0.0.1 2016年12月29日 下午2:35:00 shimingliu
 */
@SalukiService
public class HealthImpl implements Health {

    private AbstractApplicationContext applicationContext;

    public HealthImpl(AbstractApplicationContext applicationContext){
        this.applicationContext = applicationContext;
    }

    /**
     * <strong>描述：提供健康检查</strong>TODO 描述 <br>
     * <strong>功能：</strong><br>
     * <strong>使用场景：</strong><br>
     * <strong>注意事项：</strong>
     * <ul>
     * <li></li>
     * </ul>
     * 
     * @see com.quancheng.saluki.service.Health#Check(com.quancheng.saluki.service.serviceparam.HealthCheckRequest)
     */
    @Override
    public HealthCheckResponse Check(HealthCheckRequest healthcheckrequest) {
        String service = healthcheckrequest.getService();
        try {
            Object obj = applicationContext.getBeansOfType(ReflectUtils.name2class(service));
            if (obj != null) {
                HealthCheckResponse response = new HealthCheckResponse();
                response.setStatus(com.quancheng.saluki.service.serviceparam.ServingStatus.SERVING);
                return response;
            } else {
                HealthCheckResponse response = new HealthCheckResponse();
                response.setStatus(com.quancheng.saluki.service.serviceparam.ServingStatus.NOT_SERVING);
                return response;
            }
        } catch (BeansException | ClassNotFoundException e) {
            HealthCheckResponse response = new HealthCheckResponse();
            response.setStatus(com.quancheng.saluki.service.serviceparam.ServingStatus.UNKNOWN);
            return response;
        }
    }

}
