/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.service.MonitorService;
import com.quancheng.saluki.domain.GrpcInvoke;
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
        if (!Constants.MONITOR_PROTOCOL.equals(statistics.getProtocol())) {
            return;
        }
        try {
            GrpcInvoke invoke = new GrpcInvoke();
            invoke.setId(MonitorUtil.createUUID());
            if (statistics.hasParameter(PROVIDER)) {
                invoke.setType(CONSUMER);
                invoke.setConsumer(statistics.getAddress());
                invoke.setProvider(statistics.getParameter(PROVIDER));
            } else {
                invoke.setType(PROVIDER);
                invoke.setConsumer(statistics.getParameter(CONSUMER));
                invoke.setProvider(statistics.getAddress());
            }
            invoke.setInvokeDate(new Date(Long.valueOf(statistics.getParameter(TIMESTAMP))));
            invoke.setApplication(statistics.getParameter(APPLICATION));
            invoke.setService(statistics.getServiceInterface());
            invoke.setMethod(statistics.getParameter(METHOD));
            invoke.setConcurrent(statistics.getParameter(CONCURRENT, 1));
            invoke.setMaxElapsed(statistics.getParameter(MAX_ELAPSED, 0));
            invoke.setMaxConcurrent(statistics.getParameter(MAX_CONCURRENT, 0));
            invoke.setMaxInput(statistics.getParameter(MAX_INPUT, 0));
            invoke.setMaxOutput(statistics.getParameter(MAX_OUTPUT, 0));
            invoke.setSuccess(statistics.getParameter(SUCCESS, 0));
            invoke.setFailure(statistics.getParameter(FAILURE, 0));
            invoke.setElapsed(statistics.getParameter(MonitorService.ELAPSED, 0));
            invoke.setInput(statistics.getParameter(MonitorService.INPUT, 0));
            invoke.setOutput(statistics.getParameter(MonitorService.OUTPUT, 0));
            if (invoke.getSuccess() == 0 && invoke.getFailure() == 0 && invoke.getElapsed() == 0
                && invoke.getConcurrent() == 0 && invoke.getMaxElapsed() == 0 && invoke.getMaxConcurrent() == 0) {
                return;
            }
            invokeMapping.addInvoke(invoke);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }

    }

}
