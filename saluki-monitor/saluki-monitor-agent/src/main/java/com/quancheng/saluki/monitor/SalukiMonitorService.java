package com.quancheng.saluki.monitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.monitor.MonitorService;
import com.quancheng.saluki.core.utils.NamedThreadFactory;
import com.quancheng.saluki.monitor.domain.SalukiInvoke;
import com.quancheng.saluki.monitor.domain.Statistics;
import com.quancheng.saluki.monitor.mapper.SalukiInvokeMapper;
import com.quancheng.saluki.monitor.util.SpringBeanUtils;
import com.quancheng.saluki.monitor.util.UuidUtil;

public class SalukiMonitorService implements MonitorService {

    private static final Logger                                      logger = LoggerFactory.getLogger(SalukiMonitorService.class);

    private static final int                                         LENGTH = 10;

    private final SalukiInvokeMapper                                 invokeMapping;

    private final ScheduledFuture<?>                                 sendFuture;

    private final ConcurrentMap<Statistics, AtomicReference<long[]>> statisticsMap;

    // 定时清理内存数据
    private final ScheduledExecutorService                           clearDataExecutor;

    // 定时收集器
    private final ScheduledExecutorService                           scheduledExecutorService;

    public SalukiMonitorService(){
        statisticsMap = Maps.newConcurrentMap();
        invokeMapping = SpringBeanUtils.getBean(SalukiInvokeMapper.class);
        clearDataExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("SalukiClearMonitorData", true));
        clearDataExecutor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                clearDataBase();

            }
        }, 0, 1, TimeUnit.DAYS);
        scheduledExecutorService = Executors.newScheduledThreadPool(3, new NamedThreadFactory("DubboMonitorSendTimer",
                                                                                              true));
        sendFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

            public void run() {
                try {
                    send();
                } catch (Throwable t) {
                    logger.error("Unexpected error occur at send statistic, cause: " + t.getMessage(), t);
                }
            }
        }, 60000, 60000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void collect(SalukiURL url) {
        // 读写统计变量
        int success = url.getParameter(MonitorService.SUCCESS, 0);
        int failure = url.getParameter(MonitorService.FAILURE, 0);
        int input = url.getParameter(MonitorService.INPUT, 0);
        int output = url.getParameter(MonitorService.OUTPUT, 0);
        int elapsed = url.getParameter(MonitorService.ELAPSED, 0);
        int concurrent = url.getParameter(MonitorService.CONCURRENT, 0);
        // 初始化原子引用
        Statistics statistics = new Statistics(url);
        AtomicReference<long[]> reference = statisticsMap.get(statistics);
        if (reference == null) {
            statisticsMap.putIfAbsent(statistics, new AtomicReference<long[]>());
            reference = statisticsMap.get(statistics);
        }
        // CompareAndSet并发加入统计数据
        long[] current;
        long[] update = new long[LENGTH];
        do {
            current = reference.get();
            if (current == null) {
                update[0] = success;
                update[1] = failure;
                update[2] = input;
                update[3] = output;
                update[4] = elapsed;
                update[5] = concurrent;
                update[6] = input;
                update[7] = output;
                update[8] = elapsed;
                update[9] = concurrent;
            } else {
                update[0] = current[0] + success;
                update[1] = current[1] + failure;
                update[2] = current[2] + input;
                update[3] = current[3] + output;
                update[4] = current[4] + elapsed;
                update[5] = (current[5] + concurrent) / 2;
                update[6] = current[6] > input ? current[6] : input;
                update[7] = current[7] > output ? current[7] : output;
                update[8] = current[8] > elapsed ? current[8] : elapsed;
                update[9] = current[9] > concurrent ? current[9] : concurrent;
            }
        } while (!reference.compareAndSet(current, update));
    }

    private void clearDataBase() {
        invokeMapping.truncateTable();
    }

    public void send() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        for (Map.Entry<Statistics, AtomicReference<long[]>> entry : statisticsMap.entrySet()) {
            // 获取已统计数据
            Statistics statistics = entry.getKey();
            AtomicReference<long[]> reference = entry.getValue();
            long[] numbers = reference.get();
            long success = numbers[0];
            long failure = numbers[1];
            long input = numbers[2];
            long output = numbers[3];
            long elapsed = numbers[4];
            long concurrent = numbers[5];
            long maxInput = numbers[6];
            long maxOutput = numbers[7];
            long maxElapsed = numbers[8];
            long maxConcurrent = numbers[9];
            // 发送汇总信息
            SalukiURL url = statistics.getUrl().addParameters(MonitorService.TIMESTAMP, timestamp,
                                                              MonitorService.SUCCESS, String.valueOf(success),
                                                              MonitorService.FAILURE, String.valueOf(failure),
                                                              MonitorService.INPUT, String.valueOf(input),
                                                              MonitorService.OUTPUT, String.valueOf(output),
                                                              MonitorService.ELAPSED, String.valueOf(elapsed),
                                                              MonitorService.CONCURRENT, String.valueOf(concurrent),
                                                              MonitorService.MAX_INPUT, String.valueOf(maxInput),
                                                              MonitorService.MAX_OUTPUT, String.valueOf(maxOutput),
                                                              MonitorService.MAX_ELAPSED, String.valueOf(maxElapsed),
                                                              MonitorService.MAX_CONCURRENT,
                                                              String.valueOf(maxConcurrent));
            writeToDataBase(url);
            // 减掉已统计数据
            long[] current;
            long[] update = new long[LENGTH];
            do {
                current = reference.get();
                if (current == null) {
                    update[0] = 0;
                    update[1] = 0;
                    update[2] = 0;
                    update[3] = 0;
                    update[4] = 0;
                    update[5] = 0;
                } else {
                    update[0] = current[0] - success;
                    update[1] = current[1] - failure;
                    update[2] = current[2] - input;
                    update[3] = current[3] - output;
                    update[4] = current[4] - elapsed;
                    update[5] = current[5] - concurrent;
                }
            } while (!reference.compareAndSet(current, update));
        }
    }

    private void writeToDataBase(SalukiURL statistics) {
        if (!SalukiConstants.MONITOR_PROTOCOL.equals(statistics.getProtocol())) {
            return;
        }
        String timestamp = statistics.getParameter(TIMESTAMP);
        Date invokeTime = null;
        if (timestamp == null || timestamp.length() == 0) {
            invokeTime = new Date();
        } else if (timestamp.length() == "yyyyMMddHHmmss".length()) {
            try {
                invokeTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(timestamp);
            } catch (ParseException e) {
                invokeTime = new Date();
            }
        } else {
            invokeTime = new Date(Long.parseLong(timestamp));
        }
        SalukiInvoke invoke = new SalukiInvoke();
        invoke.setId(UuidUtil.createUUID());
        try {
            if (statistics.hasParameter(PROVIDER)) {
                invoke.setType(CONSUMER);
                invoke.setConsumer(statistics.getHost());
                invoke.setProvider(statistics.getParameter(PROVIDER));
            } else {
                invoke.setType(PROVIDER);
                invoke.setConsumer(statistics.getParameter(CONSUMER));
                invoke.setProvider(statistics.getHost());
            }
            invoke.setInvokeDate(invokeTime);
            invoke.setService(statistics.getServiceInterface());
            invoke.setMethod(statistics.getParameter(METHOD));
            invoke.setInvokeTime(statistics.getParameter(TIMESTAMP, System.currentTimeMillis()));
            invoke.setSuccess(statistics.getParameter(SUCCESS, 0));
            invoke.setFailure(statistics.getParameter(FAILURE, 0));
            invoke.setElapsed(statistics.getParameter(ELAPSED, 0));
            invoke.setConcurrent(statistics.getParameter(CONCURRENT, 0));
            if (invoke.getSuccess() == 0 && invoke.getFailure() == 0 && invoke.getElapsed() == 0
                && invoke.getConcurrent() == 0) {
                return;
            }
            invokeMapping.addInvoke(invoke);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }

    public void destroy() {
        try {
            sendFuture.cancel(true);
        } catch (Throwable t) {
            logger.error("Unexpected error occur at cancel sender timer, cause: " + t.getMessage(), t);
        }
    }
}
