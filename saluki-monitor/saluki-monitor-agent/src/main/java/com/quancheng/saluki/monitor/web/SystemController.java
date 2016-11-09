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

@RestController
@RequestMapping("/salukiMonitor")
public class SystemController {

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");

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

        rows.add(new String[] { "Uptime", formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()) });

        rows.add(new String[] { "Time", formatter.format(new Date()) });

        Map<String, Object> model = Maps.newHashMap();
        model.put("rows", rows);
        return model;
    }

    private static final long SECOND = 1000;

    private static final long MINUTE = 60 * SECOND;

    private static final long HOUR   = 60 * MINUTE;

    private static final long DAY    = 24 * HOUR;

    private String formatUptime(long uptime) {
        StringBuilder buf = new StringBuilder();
        if (uptime > DAY) {
            long days = (uptime - uptime % DAY) / DAY;
            buf.append(days);
            buf.append(" Days");
            uptime = uptime % DAY;
        }
        if (uptime > HOUR) {
            long hours = (uptime - uptime % HOUR) / HOUR;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(hours);
            buf.append(" Hours");
            uptime = uptime % HOUR;
        }
        if (uptime > MINUTE) {
            long minutes = (uptime - uptime % MINUTE) / MINUTE;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(minutes);
            buf.append(" Minutes");
            uptime = uptime % MINUTE;
        }
        if (uptime > SECOND) {
            long seconds = (uptime - uptime % SECOND) / SECOND;
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(seconds);
            buf.append(" Seconds");
            uptime = uptime % SECOND;
        }
        if (uptime > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(uptime);
            buf.append(" Milliseconds");
        }
        return buf.toString();
    }

}
