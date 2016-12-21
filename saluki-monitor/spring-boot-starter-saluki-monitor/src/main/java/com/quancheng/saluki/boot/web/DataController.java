package com.quancheng.saluki.boot.web;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.quancheng.saluki.boot.autoconfigure.GrpcProperties;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.core.utils.NetUtils;
import com.quancheng.saluki.core.utils.Version;
import com.quancheng.saluki.domain.GrpcInvoke;
import com.quancheng.saluki.monitor.InvokeMapper;
import com.quancheng.saluki.monitor.MonitorUtil;

@RestController
@RequestMapping("monitor")
public class DataController {

    private SimpleDateFormat               formatter         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");

    private final InvokeMapper             mapper            = MonitorUtil.getBean(InvokeMapper.class);

    private final ScheduledExecutorService scheduleClearData = Executors.newScheduledThreadPool(1,
                                                                                                new NamedThreadFactory("ClearMonitorData",
                                                                                                                       true));
    @Autowired
    private GrpcProperties               thrallProperties;

    public void init() {
        scheduleClearData.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                clearData();
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    @RequestMapping(value = "/system", method = RequestMethod.GET)
    public Map<String, Object> system() {
        List<String[]> rows = new ArrayList<String[]>();
        rows.add(new String[] { "Saluki_Version", Version.getVersion() });
        rows.add(new String[] { "Host",
                                thrallProperties.getHost() != null ? thrallProperties.getHost() : NetUtils.getLocalHost() });
        rows.add(new String[] { "OS", System.getProperty("os.name") + " " + System.getProperty("os.version") });
        rows.add(new String[] { "JVM",
                                System.getProperty("java.runtime.name") + " "
                                       + System.getProperty("java.runtime.version") + ",<br/>"
                                       + System.getProperty("java.vm.name") + " "
                                       + System.getProperty("java.vm.version") + " "
                                       + System.getProperty("java.vm.info", "") });

        rows.add(new String[] { "CPU", System.getProperty("os.arch", "") + ", "
                                       + String.valueOf(Runtime.getRuntime().availableProcessors()) + " cores" });

        rows.add(new String[] { "Locale", Locale.getDefault().toString() + "/" + System.getProperty("file.encoding") });

        rows.add(new String[] { "Uptime", MonitorUtil.formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()) });

        rows.add(new String[] { "Time", formatter.format(new Date()) });

        Map<String, Object> model = Maps.newHashMap();
        model.put("rows", rows);
        return model;
    }

    @RequestMapping(value = "statistics", method = RequestMethod.GET)
    public List<GrpcInvoke> statistics() {
        List<GrpcInvoke> statistics = mapper.queryData();
        return statistics;
    }

    @RequestMapping(value = "/clean", method = RequestMethod.GET)
    public void cleanData() {
        clearData();
    }

    private void clearData() {
        mapper.truncateTable();
    }

}
