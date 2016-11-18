package com.quancheng.saluki.monitor;

import java.math.BigDecimal;
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
import com.quancheng.saluki.monitor.domain.SalukiInvokeStatistics;
import com.quancheng.saluki.monitor.domain.Statistics;
import com.quancheng.saluki.monitor.mapper.SalukiInvokeMapper;
import com.quancheng.saluki.monitor.util.SpringBeanUtils;
import com.quancheng.saluki.monitor.util.UuidUtil;

public class SalukiMonitorService implements MonitorService {

    private static final Logger                                      logger = LoggerFactory.getLogger(SalukiMonitorService.class);

    private static final int                                         LENGTH = 11;

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
        long timestamp = url.getParameter(MonitorService.TIMESTAMP, 0L);
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
                update[10] = timestamp;
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
                update[10] = (current[10] + timestamp) / 2;
            }
        } while (!reference.compareAndSet(current, update));
    }

    private void clearDataBase() {
        invokeMapping.truncateTable();
    }

    public void send() {
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
            long timestamp = numbers[10];
            // 发送汇总信息
            SalukiURL url = statistics.getUrl().addParameters(MonitorService.TIMESTAMP, String.valueOf(timestamp),
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
        try {
            SalukiInvokeStatistics invoke = new SalukiInvokeStatistics();
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
            invoke.setService(statistics.getServiceInterface());
            invoke.setMethod(statistics.getParameter(METHOD));
            invoke.setConcurrent(statistics.getParameter(CONCURRENT, 0));
            invoke.setMaxElapsed(statistics.getParameter(MAX_ELAPSED, 0));
            invoke.setMaxConcurrent(statistics.getParameter(MAX_CONCURRENT, 0));
            invoke.setMaxInput(statistics.getParameter(MAX_INPUT, 0));
            invoke.setMaxOutput(statistics.getParameter(MAX_OUTPUT, 0));
            if (invoke.getSuccess() == 0 && invoke.getFailure() == 0 && invoke.getElapsed() == 0
                && invoke.getConcurrent() == 0 && invoke.getMaxElapsed() == 0 && invoke.getMaxConcurrent() == 0) {
                return;
            }
            // 计算统计信息
            int failureCount = statistics.getParameter(FAILURE, 0);
            int successCount = statistics.getParameter(SUCCESS, 0);
            int totalCount = failureCount + successCount;
            invoke.setSuccess(successCount);
            invoke.setFailure(failureCount);
            invoke.setElapsed(Double.valueOf(statistics.getParameter(ELAPSED, 0) / totalCount));
            invoke.setInput(Double.valueOf(statistics.getParameter(INPUT, 0) / totalCount));
            invoke.setOutput(Double.valueOf(statistics.getParameter(OUTPUT, 0) / totalCount));
            if (invoke.getElapsed() != 0) {
                // TPS=并发数/响应时间
                BigDecimal tps = new BigDecimal(invoke.getConcurrent());
                tps = tps.divide(BigDecimal.valueOf(invoke.getElapsed()), 2, BigDecimal.ROUND_HALF_DOWN);
                tps = tps.multiply(BigDecimal.valueOf(1000));
                // 每秒能够处理的请求数量
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

    public void destroy() {
        try {
            sendFuture.cancel(true);
        } catch (Throwable t) {
            logger.error("Unexpected error occur at cancel sender timer, cause: " + t.getMessage(), t);
        }
    }
}
