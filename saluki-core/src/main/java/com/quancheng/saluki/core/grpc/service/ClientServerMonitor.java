package com.quancheng.saluki.core.grpc.service;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.utils.ClassHelper;

public class ClientServerMonitor implements MonitorService {

    private static final Logger                                      logger                   = LoggerFactory.getLogger(ClientServerMonitor.class);

    private static final int                                         LENGTH                   = 10;

    private final ScheduledFuture<?>                                 sendFuture;

    private final List<MonitorService>                               monitorServices;

    private final long                                               monitorInterval;

    private final ScheduledExecutorService                           scheduledExecutorService = Executors.newScheduledThreadPool(1,
                                                                                                                                 new NamedThreadFactory("SalukiMonitorSendTimer",
                                                                                                                                                        true));

    private final ConcurrentMap<Statistics, AtomicReference<long[]>> statisticsMap            = new ConcurrentHashMap<Statistics, AtomicReference<long[]>>();

    public ClientServerMonitor(GrpcURL url){
        this.monitorServices = findMonitor();
        this.monitorInterval = url.getParameter("monitorinterval", 60);
        // 启动统计信息收集定时器
        sendFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

            public void run() {
                // 收集统计信息
                try {
                    send();
                } catch (Throwable t) { // 防御性容错
                    logger.error("Unexpected error occur at send statistic, cause: " + t.getMessage(), t);
                }
            }
        }, monitorInterval, monitorInterval, TimeUnit.MINUTES);
    }

    public void send() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        for (Map.Entry<Statistics, AtomicReference<long[]>> entry : statisticsMap.entrySet()) {
            // 获取已统计数据
            Statistics statistics = entry.getKey();
            AtomicReference<long[]> reference = entry.getValue();
            long[] numbers = reference.get();
            // 如果是0，需要等下次的数据
            if (!isZero(numbers)) {
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
                GrpcURL url = statistics.getUrl().addParameters(MonitorService.TIMESTAMP, String.valueOf(timestamp),
                                                                MonitorService.SUCCESS, String.valueOf(success),
                                                                MonitorService.FAILURE, String.valueOf(failure),
                                                                MonitorService.INPUT, String.valueOf(input),
                                                                MonitorService.OUTPUT, String.valueOf(output),
                                                                MonitorService.ELAPSED, String.valueOf(elapsed),
                                                                MonitorService.CONCURRENT,
                                                                String.valueOf(concurrent / (success + failure)),
                                                                MonitorService.MAX_INPUT, String.valueOf(maxInput),
                                                                MonitorService.MAX_OUTPUT, String.valueOf(maxOutput),
                                                                MonitorService.MAX_ELAPSED, String.valueOf(maxElapsed),
                                                                MonitorService.MAX_CONCURRENT,
                                                                String.valueOf(maxConcurrent));
                for (MonitorService monitor : monitorServices) {
                    monitor.collect(url);
                }
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
    }

    @Override
    public void collect(GrpcURL url) {
        // 读写统计变量
        int success = url.getParameter(MonitorService.SUCCESS, 0);
        int failure = url.getParameter(MonitorService.FAILURE, 0);
        int input = url.getParameter(MonitorService.INPUT, 0);
        int output = url.getParameter(MonitorService.OUTPUT, 0);
        int elapsed = url.getParameter(MonitorService.ELAPSED, 0);
        int concurrent = url.getParameter(MonitorService.CONCURRENT, 1);
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
                update[5] = current[5] + concurrent;
                update[6] = current[6] > input ? current[6] : input;
                update[7] = current[7] > output ? current[7] : output;
                update[8] = current[8] > elapsed ? current[8] : elapsed;
                update[9] = current[9] > concurrent ? current[9] : concurrent;
            }
        } while (!reference.compareAndSet(current, update));
    }

    private boolean isZero(long[] current) {
        return current[0] == 0l && current[1] == 0l && current[2] == 0l && current[3] == 0l && current[4] == 0l
               && current[5] == 0l && current[6] == 0l && current[7] == 0l && current[8] == 0l && current[9] == 0l;
    }

    public void destroy() {
        try {
            sendFuture.cancel(true);
        } catch (Throwable t) {
            logger.error("Unexpected error occur at cancel sender timer, cause: " + t.getMessage(), t);
        }
    }

    private List<MonitorService> findMonitor() {
        Iterable<MonitorService> candidates = ServiceLoader.load(MonitorService.class, ClassHelper.getClassLoader());
        List<MonitorService> list = Lists.newArrayList();
        for (MonitorService current : candidates) {
            list.add(current);
        }
        return list;
    }

}
