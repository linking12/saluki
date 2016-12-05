package com.quancheng.saluki.monitor;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.monitor.MonitorService;
import com.quancheng.saluki.monitor.common.SpringBeanUtils;
import com.quancheng.saluki.monitor.common.UuidUtil;
import com.quancheng.saluki.monitor.repository.SalukiInvokeMapper;

public class SalukiMonitorService implements MonitorService {

    private static final Logger      logger = LoggerFactory.getLogger(SalukiMonitorService.class);

    private final SalukiInvokeMapper invokeMapping;

    public SalukiMonitorService(){
        invokeMapping = SpringBeanUtils.getBean(SalukiInvokeMapper.class);
    }

    @Override
    public void collect(SalukiURL statistics) {
        if (!SalukiConstants.MONITOR_PROTOCOL.equals(statistics.getProtocol())) {
            return;
        }
        try {
            SalukiInvoke invoke = new SalukiInvoke();
            invoke.setId(UuidUtil.createUUID());
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

    public void clearDataBase() {
        invokeMapping.truncateTable();
    }

}
