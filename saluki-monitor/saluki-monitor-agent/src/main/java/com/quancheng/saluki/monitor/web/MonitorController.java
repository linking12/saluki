package com.quancheng.saluki.monitor.web;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.saluki.monitor.domain.SalukiInvoke;
import com.quancheng.saluki.monitor.mapper.SalukiInvokeMapper;
import com.quancheng.saluki.monitor.util.SpringBeanUtils;

@RestController
@RequestMapping("/salukiMonitor/all")
public class MonitorController {

    private final SalukiInvokeMapper mapper = SpringBeanUtils.getBean(SalukiInvokeMapper.class);

    @RequestMapping(value = "/queryByService", method = RequestMethod.GET)
    public List<SalukiInvoke> view(@RequestParam(value = "service", required = true) String service) {
        if (StringUtils.isNoneBlank(service)) {
            return mapper.queryAllInvoke(service);
        } else {
            return Collections.emptyList();
        }
    }

}
