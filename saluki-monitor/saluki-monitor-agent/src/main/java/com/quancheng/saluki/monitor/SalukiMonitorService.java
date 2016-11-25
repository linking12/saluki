package com.quancheng.saluki.monitor;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.monitor.MonitorService;
import com.quancheng.saluki.monitor.mapper.SalukiInvokeMapper;
import com.quancheng.saluki.monitor.util.SpringBeanUtils;
import com.quancheng.saluki.monitor.util.UuidUtil;

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
                invoke.setConsumer(statistics.getHost());
                invoke.setProvider(statistics.getParameter(PROVIDER));
            } else {
                invoke.setType(PROVIDER);
                invoke.setConsumer(statistics.getParameter(CONSUMER));
                invoke.setProvider(statistics.getHost());
            }
            invoke.setInvokeDate(new Date(Long.valueOf(statistics.getParameter(TIMESTAMP))));
            invoke.setInvokeTime(Long.valueOf(statistics.getParameter(TIMESTAMP)));
            invoke.setService(statistics.getServiceInterface());
            invoke.setMethod(statistics.getParameter(METHOD));
            invoke.setConcurrent(statistics.getParameter(CONCURRENT, 1));
            invoke.setMaxElapsed(statistics.getParameter(MAX_ELAPSED, 0));
            invoke.setMaxConcurrent(statistics.getParameter(MAX_CONCURRENT, 0));
            invoke.setMaxInput(statistics.getParameter(MAX_INPUT, 0));
            invoke.setMaxOutput(statistics.getParameter(MAX_OUTPUT, 0));
            invoke.setSuccess(statistics.getParameter(SUCCESS, 0));
            invoke.setFailure(statistics.getParameter(FAILURE, 0));
            int totalCount = invoke.getSuccess() + invoke.getFailure();
            if (totalCount <= 0) {
                return;
            }
            /**
             * elapsed:平均响应时间<br/>
             * input:平均数据请求量<br/>
             * output:平均数据返回量<br/>
             */
            invoke.setElapsed(Double.valueOf(statistics.getParameter(MonitorService.ELAPSED, 0) / totalCount));
            invoke.setInput(Double.valueOf(statistics.getParameter(MonitorService.INPUT, 0) / totalCount));
            invoke.setOutput(Double.valueOf(statistics.getParameter(MonitorService.OUTPUT, 0) / totalCount));
            if (invoke.getElapsed() != 0) {
                // TPS=并发数/平均响应时间
                BigDecimal tps = new BigDecimal(invoke.getConcurrent());
                tps = tps.divide(BigDecimal.valueOf(invoke.getElapsed()), 2, BigDecimal.ROUND_HALF_DOWN);
                tps = tps.multiply(BigDecimal.valueOf(1000));
                invoke.setTps(tps.doubleValue());
            }
            BigDecimal kbps = new BigDecimal(invoke.getTps());
            if (invoke.getInput() != 0 && invoke.getElapsed() != 0) {
                // kbps=tps*平均每次传输的数据量
                kbps = kbps.multiply(BigDecimal.valueOf(invoke.getInput()).divide(BigDecimal.valueOf(1024), 2,
                                                                                  BigDecimal.ROUND_HALF_DOWN));
            } else if (invoke.getElapsed() != 0) {
                kbps = kbps.multiply(BigDecimal.valueOf(invoke.getOutput()).divide(BigDecimal.valueOf(1024), 2,
                                                                                   BigDecimal.ROUND_HALF_DOWN));
            }
            invoke.setKbps(kbps.doubleValue());
            invokeMapping.addInvoke(invoke);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }

    public void clearDataBase() {
        invokeMapping.truncateTable();
    }

}
