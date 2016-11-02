package com.quancheng.saluki.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.monitor.MonitorService;
import com.quancheng.saluki.monitor.domain.DubboInvoke;
import com.quancheng.saluki.monitor.mapper.DubboInvokeMapper;
import com.quancheng.saluki.monitor.util.SpringBeanUtils;
import com.quancheng.saluki.monitor.util.UuidUtil;

public class SalukiMonitorService implements MonitorService {

    private static final Logger            logger  = LoggerFactory.getLogger(SalukiMonitorService.class);

    private final BlockingQueue<SalukiURL> queue;

    private final Thread                   writeThread;

    private final DubboInvokeMapper        invokeMapping;

    private volatile boolean               running = true;

    public SalukiMonitorService(){
        queue = new LinkedBlockingQueue<SalukiURL>(100000);
        invokeMapping = SpringBeanUtils.getBean(DubboInvokeMapper.class);
        writeThread = new Thread(new Runnable() {

            public void run() {
                while (running) {
                    try {
                        writeToDataBase();
                    } catch (Throwable t) {
                        logger.error("Unexpected error occur at write stat log, cause: " + t.getMessage(), t);
                        try {
                            Thread.sleep(5000);
                        } catch (Throwable t2) {
                        }
                    }
                }
            }
        });
        writeThread.setDaemon(true);
        writeThread.setName("DubboMonitorAsyncWriteLogThread");
        writeThread.start();
    }

    @Override
    public void collect(SalukiURL statistics) {
        queue.offer(statistics);
        if (logger.isDebugEnabled()) {
            logger.debug("collect statistics: " + statistics);
        }
    }

    @Override
    public List<SalukiURL> lookup(SalukiURL statistics) {
        return null;
    }

    private void writeToDataBase() throws Exception {
        SalukiURL statistics = queue.take();
        if (!SalukiConstants.MONITOR_PROTOCOL.equals(statistics.getProtocol())) {
            return;
        }
        String timestamp = statistics.getParameter(TIMESTAMP);
        Date now;
        if (timestamp == null || timestamp.length() == 0) {
            now = new Date();
        } else if (timestamp.length() == "yyyyMMddHHmmss".length()) {
            now = new SimpleDateFormat("yyyyMMddHHmmss").parse(timestamp);
        } else {
            now = new Date(Long.parseLong(timestamp));
        }
        DubboInvoke dubboInvoke = new DubboInvoke();
        dubboInvoke.setId(UuidUtil.createUUID());
        try {
            if (statistics.hasParameter(PROVIDER)) {
                dubboInvoke.setType(CONSUMER);
                dubboInvoke.setConsumer(statistics.getHost());
                dubboInvoke.setProvider(statistics.getParameter(PROVIDER));
            } else {
                dubboInvoke.setType(PROVIDER);
                dubboInvoke.setConsumer(statistics.getParameter(CONSUMER));
                dubboInvoke.setProvider(statistics.getHost());
            }
            dubboInvoke.setInvokeDate(now);
            dubboInvoke.setService(statistics.getServiceInterface());
            dubboInvoke.setMethod(statistics.getParameter(METHOD));
            dubboInvoke.setInvokeTime(statistics.getParameter(TIMESTAMP, System.currentTimeMillis()));
            dubboInvoke.setSuccess(statistics.getParameter(SUCCESS, 0));
            dubboInvoke.setFailure(statistics.getParameter(FAILURE, 0));
            dubboInvoke.setElapsed(statistics.getParameter(ELAPSED, 0));
            dubboInvoke.setConcurrent(statistics.getParameter(CONCURRENT, 0));
            if (dubboInvoke.getSuccess() == 0 && dubboInvoke.getFailure() == 0 && dubboInvoke.getElapsed() == 0
                && dubboInvoke.getConcurrent() == 0 && dubboInvoke.getMaxElapsed() == 0
                && dubboInvoke.getMaxConcurrent() == 0) {
                return;
            }
            invokeMapping.addDubboInvoke(dubboInvoke);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }
}
