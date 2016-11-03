package com.quancheng.saluki.monitor.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.quancheng.saluki.monitor.domain.LineChartSeries;
import com.quancheng.saluki.monitor.domain.SalukiInvoke;
import com.quancheng.saluki.monitor.domain.SalukiInvokeLineChart;
import com.quancheng.saluki.monitor.util.CommonResponse;
import com.quancheng.saluki.monitor.util.DateUtil;

@RestController
@RequestMapping("/salukiMonitor")
public class IndexController {

    @Autowired
    private SalukiMonitoWebService dubboMonitorService;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public CommonResponse loadTopDate(@RequestParam(value = "from", required = true) String from,
                                      @RequestParam(value = "to", required = true) String to) {
        SalukiInvoke dubboInvoke = new SalukiInvoke();
        dubboInvoke.setInvokeDateFrom(DateUtil.parse(from));
        dubboInvoke.setInvokeDateTo(DateUtil.parse(to));
        CommonResponse commonResponse = CommonResponse.createCommonResponse();
        List<SalukiInvokeLineChart> dubboInvokeLineChartList = new ArrayList<SalukiInvokeLineChart>();
        SalukiInvokeLineChart successDubboInvokeLineChart = new SalukiInvokeLineChart();
        List<String> sxAxisCategories = Lists.newArrayList();
        LineChartSeries slineChartSeries = new LineChartSeries();
        List<double[]> sdataList = Lists.newArrayList();
        double[] data;
        Map<String, List<SalukiInvoke>> dubboInvokeMap = dubboMonitorService.countDubboInvokeTopTen(dubboInvoke);
        List<SalukiInvoke> success = (List<SalukiInvoke>) dubboInvokeMap.get("success");
        for (SalukiInvoke di : success) {
            sxAxisCategories.add(di.getMethod());
            data = new double[] { di.getSuccess() };
            sdataList.add(data);
        }
        slineChartSeries.setData(sdataList);
        slineChartSeries.setName("service");
        successDubboInvokeLineChart.setxAxisCategories(sxAxisCategories);
        successDubboInvokeLineChart.setSeriesData(Arrays.asList(slineChartSeries));
        successDubboInvokeLineChart.setChartType("SUCCESS");
        successDubboInvokeLineChart.setTitle("The Top 20 of Invoke Success");
        successDubboInvokeLineChart.setyAxisTitle("t");
        dubboInvokeLineChartList.add(successDubboInvokeLineChart);
        SalukiInvokeLineChart failureDubboInvokeLineChart = new SalukiInvokeLineChart();
        List<String> fxAxisCategories = Lists.newArrayList();
        LineChartSeries flineChartSeries = new LineChartSeries();
        List<double[]> fdataList = Lists.newArrayList();
        List<SalukiInvoke> failure = (List<SalukiInvoke>) dubboInvokeMap.get("failure");
        for (SalukiInvoke di : failure) {
            fxAxisCategories.add(di.getMethod());
            data = new double[] { di.getFailure() };
            fdataList.add(data);
        }
        flineChartSeries.setData(fdataList);
        flineChartSeries.setName("service");
        failureDubboInvokeLineChart.setxAxisCategories(fxAxisCategories);
        failureDubboInvokeLineChart.setSeriesData(Arrays.asList(flineChartSeries));
        failureDubboInvokeLineChart.setChartType("FAILURE");
        failureDubboInvokeLineChart.setTitle("The Top 20 of Invoke Failure");
        failureDubboInvokeLineChart.setyAxisTitle("t");
        dubboInvokeLineChartList.add(failureDubboInvokeLineChart);
        commonResponse.setData(dubboInvokeLineChartList);
        return commonResponse;
    }
}
