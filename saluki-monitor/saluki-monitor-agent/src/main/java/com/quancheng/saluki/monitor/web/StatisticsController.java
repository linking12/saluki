package com.quancheng.saluki.monitor.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Maps;
import com.quancheng.saluki.monitor.domain.SalukiInvoke;
import com.quancheng.saluki.monitor.domain.SalukiStatistics;

@Controller
@RequestMapping("/salukiMonitor/statistics")
public class StatisticsController {

    @Autowired
    private SalukiMonitoWebService dubboMonitorService;

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> index() {
        SalukiInvoke dubboInvoke = new SalukiInvoke();
        dubboInvoke.setInvokeDate(new Date());
        List<String> methods = dubboMonitorService.getMethodsByService(dubboInvoke);
        List<SalukiInvoke> dubboInvokes;
        List<SalukiStatistics> dubboStatisticses = new ArrayList<SalukiStatistics>();
        SalukiStatistics dubboStatistics;
        for (String method : methods) {
            dubboStatistics = new SalukiStatistics();
            dubboStatistics.setMethod(method);
            dubboInvoke.setMethod(method);
            dubboInvokes = dubboMonitorService.countDubboInvokeInfo(dubboInvoke);
            for (SalukiInvoke di : dubboInvokes) {
                if (di == null) {
                    continue;
                }
                dubboStatistics.setSuccess(di.getSuccess());
                dubboStatistics.setFailure(di.getFailure());
                dubboStatistics.setAvgElapsed(di.getSuccess() != 0 ? Double.valueOf(String.format("%.4f",
                                                                                                  di.getElapsed()
                                                                                                          / di.getSuccess())) : 0);
            }
        }
        Map<String, Object> model = Maps.newHashMap();
        model.put("rows", dubboStatisticses);
        model.put("service", dubboInvoke.getService());
        return model;
    }

}
