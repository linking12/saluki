/**
 * Copyright 2006-2015 handu.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.saluki.monitor.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.quancheng.saluki.monitor.domain.SalukiInvokeLineChart;
import com.quancheng.saluki.monitor.domain.LineChartSeries;
import com.quancheng.saluki.monitor.domain.SalukiInvoke;
import com.quancheng.saluki.monitor.util.CommonResponse;

/**
 * Home Controller
 *
 * @author Silence <me@chenzhiguo.cn> Created on 15/6/26.
 */
@Controller
@RequestMapping("/salukiMonitor")
public class IndexController {

    @Autowired
    private SalukiMonitoWebService dubboMonitorService;

    @RequestMapping(method = RequestMethod.GET)
    public String home() {
        return "index";
    }

    @RequestMapping(value = "index", method = RequestMethod.GET)
    public String index() {
        return "index";
    }

    @ResponseBody
    @RequestMapping(value = "loadTopData")
    public CommonResponse loadTopDate(@ModelAttribute SalukiInvoke dubboInvoke) {
        CommonResponse commonResponse = CommonResponse.createCommonResponse();
        List<SalukiInvokeLineChart> dubboInvokeLineChartList = new ArrayList<SalukiInvokeLineChart>();
        SalukiInvokeLineChart successDubboInvokeLineChart = new SalukiInvokeLineChart();
        List<String> sxAxisCategories = Lists.newArrayList();
        LineChartSeries slineChartSeries = new LineChartSeries();
        List<double[]> sdataList = Lists.newArrayList();
        double[] data;
        Map dubboInvokeMap = dubboMonitorService.countDubboInvokeTopTen(dubboInvoke);
        List<SalukiInvoke> success = (List<SalukiInvoke>) dubboInvokeMap.get("success");
        for (SalukiInvoke di : success) {
            sxAxisCategories.add(di.getMethod());
            data = new double[] { di.getSuccess() };
            sdataList.add(data);
        }
        slineChartSeries.setData(sdataList);
        slineChartSeries.setName("provider");

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
        flineChartSeries.setName("provider");

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
