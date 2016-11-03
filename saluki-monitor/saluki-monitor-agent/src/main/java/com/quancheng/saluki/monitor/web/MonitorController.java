package com.quancheng.saluki.monitor.web;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.saluki.monitor.domain.SalukiInvoke;
import com.quancheng.saluki.monitor.mapper.SalukiInvokeMapper;
import com.quancheng.saluki.monitor.util.SpringBeanUtils;

@RestController
@RequestMapping("/salukiMonitor")
public class MonitorController {

    private final SalukiInvokeMapper mapper = SpringBeanUtils.getBean(SalukiInvokeMapper.class);

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<SalukiInvoke> view() {
        return mapper.queryAllInvoke();
    }

    @RequestMapping(value = "/clear", method = RequestMethod.GET)
    public void clear(@RequestParam(value = "service", required = true) String service) {
        mapper.truncateTable();
    }
}
