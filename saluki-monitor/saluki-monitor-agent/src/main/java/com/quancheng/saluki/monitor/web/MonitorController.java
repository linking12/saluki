package com.quancheng.saluki.monitor.web;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.quancheng.saluki.core.utils.NetUtils;
import com.quancheng.saluki.monitor.mapper.SalukiInvokeMapper;
import com.quancheng.saluki.monitor.util.DateUtil;
import com.quancheng.saluki.monitor.util.SpringBeanUtils;

@RestController
@RequestMapping("/salukiMonitor")
public class MonitorController {

    private SimpleDateFormat         formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");

    private final SalukiInvokeMapper mapper    = SpringBeanUtils.getBean(SalukiInvokeMapper.class);

    @RequestMapping(value = "/system", method = RequestMethod.GET)
    public Map<String, Object> system() {
        List<String[]> rows = new ArrayList<String[]>();
        rows.add(new String[] { "Saluki_Version", "1.1" });
        String address = NetUtils.getLocalHost();
        rows.add(new String[] { "Host", NetUtils.getHostName(address) + "/" + address });
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

        rows.add(new String[] { "Uptime", DateUtil.formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()) });

        rows.add(new String[] { "Time", formatter.format(new Date()) });

        Map<String, Object> model = Maps.newHashMap();
        model.put("rows", rows);
        return model;
    }

}
