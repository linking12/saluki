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
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quancheng.saluki.monitor.domain.SalukiStatistics;
import com.quancheng.saluki.monitor.domain.SalukiInvoke;

/**
 * Statistics Controller
 *
 * @author Zhiguo.Chen <me@chenzhiguo.cn> Created on 15/7/2.
 */
@Controller
@RequestMapping("/salukiMonitor/statistics")
public class StatisticsController {

    @Autowired
    private SalukiMonitoWebService dubboMonitorService;

    @RequestMapping()
    public String index(@ModelAttribute SalukiInvoke dubboInvoke, Model model) {
        // Set default Search Date
        if (dubboInvoke.getInvokeDate() == null && dubboInvoke.getInvokeDateFrom() == null
            && dubboInvoke.getInvokeDateTo() == null) {
            dubboInvoke.setInvokeDate(new Date());
        }
        // 获取Service方法
        List<String> methods = dubboMonitorService.getMethodsByService(dubboInvoke);
        List<SalukiInvoke> dubboInvokes;
        List<SalukiStatistics> dubboStatisticses = new ArrayList<SalukiStatistics>();
        SalukiStatistics dubboStatistics;
        for (String method : methods) {
            dubboStatistics = new SalukiStatistics();
            dubboStatistics.setMethod(method);
            dubboInvoke.setMethod(method);
            dubboInvoke.setType("provider");
            dubboInvokes = dubboMonitorService.countDubboInvokeInfo(dubboInvoke);
            for (SalukiInvoke di : dubboInvokes) {
                if (di == null) {
                    continue;
                }
                dubboStatistics.setProviderSuccess(di.getSuccess());
                dubboStatistics.setProviderFailure(di.getFailure());
                dubboStatistics.setProviderAvgElapsed(di.getSuccess() != 0 ? Double.valueOf(String.format("%.4f",
                                                                                                          di.getElapsed()
                                                                                                                  / di.getSuccess())) : 0);

            }
            dubboInvoke.setType("consumer");
            dubboInvokes = dubboMonitorService.countDubboInvokeInfo(dubboInvoke);
            for (SalukiInvoke di : dubboInvokes) {
                if (di == null) {
                    continue;
                }
                dubboStatistics.setConsumerSuccess(di.getSuccess());
                dubboStatistics.setConsumerFailure(di.getFailure());
                dubboStatistics.setConsumerAvgElapsed(di.getSuccess() != 0 ? Double.valueOf(String.format("%.4f",
                                                                                                          di.getElapsed()
                                                                                                                  / di.getSuccess())) : 0);

            }
            dubboStatisticses.add(dubboStatistics);
        }
        model.addAttribute("rows", dubboStatisticses);
        model.addAttribute("service", dubboInvoke.getService());
        return "service/statistics";
    }

}
