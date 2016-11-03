package com.quancheng.saluki.monitor.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.saluki.monitor.domain.LineChartSeries;
import com.quancheng.saluki.monitor.domain.SalukiInvoke;
import com.quancheng.saluki.monitor.domain.SalukiInvokeLineChart;
import com.quancheng.saluki.monitor.util.CommonResponse;

@RestController
@RequestMapping("/salukiMonitor/charts")
public class ChartsController {

    @Autowired
    private SalukiMonitoWebService dubboMonitorService;

    @RequestMapping(value = "loadChartsData")
    public CommonResponse loadChartsData(@RequestParam(value = "service", required = true) String service,
                                         @RequestParam(value = "from", required = true) Date from,
                                         @RequestParam(value = "to", required = true) Date to) {
        SalukiInvoke dubboInvoke = new SalukiInvoke();
        dubboInvoke.setService(service);
        dubboInvoke.setInvokeDateFrom(from);
        dubboInvoke.setInvokeDateTo(to);
        long timeParticle = dubboInvoke.getTimeParticle() / 1000;
        List<SalukiInvokeLineChart> dubboInvokeLineChartList = new ArrayList<SalukiInvokeLineChart>();
        SalukiInvokeLineChart qpsLineChart;
        SalukiInvokeLineChart artLineChart;
        List<LineChartSeries> qpsLineChartSeriesList;
        List<LineChartSeries> artLineChartSeriesList;
        LineChartSeries qpsLineChartSeries;
        LineChartSeries artLineChartSeries;
        List<double[]> qpsSeriesDatas;
        List<double[]> artSeriesDatas;
        List<String> methods = dubboMonitorService.getMethodsByService(dubboInvoke);
        for (String method : methods) {
            qpsLineChart = new SalukiInvokeLineChart();
            artLineChart = new SalukiInvokeLineChart();
            qpsLineChartSeriesList = new ArrayList<LineChartSeries>();
            artLineChartSeriesList = new ArrayList<LineChartSeries>();
            dubboInvoke.setMethod(method);
            qpsLineChartSeries = new LineChartSeries();
            artLineChartSeries = new LineChartSeries();
            List<SalukiInvoke> providerDubboInvokeDetails = dubboMonitorService.countDubboInvoke(dubboInvoke);
            qpsLineChartSeries.setName(dubboInvoke.getType());
            artLineChartSeries.setName(dubboInvoke.getType());
            qpsSeriesDatas = new ArrayList<double[]>();
            artSeriesDatas = new ArrayList<double[]>();
            double[] qpsProviderSeriesData;
            double[] artProviderSeriesData;
            for (SalukiInvoke dubboInvokeDetail : providerDubboInvokeDetails) {
                qpsProviderSeriesData = new double[] { dubboInvokeDetail.getInvokeTime().getTime(),
                                                       Double.valueOf(String.format("%.4f",
                                                                                    dubboInvokeDetail.getSuccess() / timeParticle)) };
                qpsSeriesDatas.add(qpsProviderSeriesData);
                artProviderSeriesData = new double[] { dubboInvokeDetail.getInvokeTime().getTime(),
                                                       Double.valueOf(String.format("%.4f",
                                                                                    dubboInvokeDetail.getElapsed())) };
                artSeriesDatas.add(artProviderSeriesData);
            }
            qpsLineChartSeries.setData(qpsSeriesDatas);
            qpsLineChartSeriesList.add(qpsLineChartSeries);
            artLineChartSeries.setData(artSeriesDatas);
            artLineChartSeriesList.add(artLineChartSeries);
            // ====================== 统计QPS ===========================
            qpsLineChart.setSeriesData(qpsLineChartSeriesList);
            qpsLineChart.setTitle("Requests per second (QPS)");
            qpsLineChart.setyAxisTitle("t/s");
            qpsLineChart.setMethod(method);
            qpsLineChart.setChartType("QPS");
            dubboInvokeLineChartList.add(qpsLineChart);
            // ====================== 统计ART ===========================
            artLineChart.setSeriesData(artLineChartSeriesList);
            artLineChart.setTitle("Average response time (ms)");
            artLineChart.setyAxisTitle("ms/t");
            artLineChart.setMethod(method);
            artLineChart.setChartType("ART");
            dubboInvokeLineChartList.add(artLineChart);
        }
        CommonResponse commonResponse = CommonResponse.createCommonResponse();
        commonResponse.setData(dubboInvokeLineChartList);
        return commonResponse;
    }
}
