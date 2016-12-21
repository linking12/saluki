/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.service.MonitorService;
import com.quancheng.saluki.monitor.InvokeMapper;
import com.quancheng.saluki.monitor.MonitorUtil;

/**
 * @author shimingliu 2016年12月21日 下午7:15:56
 * @version MonitorService.java, v 0.0.1 2016年12月21日 下午7:15:56 shimingliu
 */
public class GrpcMonitorService implements MonitorService {

    private static final Logger logger = LoggerFactory.getLogger(GrpcMonitorService.class);
    private final InvokeMapper  invokeMapping;

    public GrpcMonitorService(){
        invokeMapping = MonitorUtil.getBean(InvokeMapper.class);
    }

    @Override
    public void collect(GrpcURL statistics) {
        // TODO Auto-generated method stub

    }

}
