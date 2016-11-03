package com.quancheng.saluki.monitor.web;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;
import com.quancheng.saluki.monitor.SalukiMonitorService;
import com.quancheng.saluki.monitor.domain.SalukiInvoke;
import com.quancheng.saluki.monitor.mapper.SalukiInvokeMapper;
import com.quancheng.saluki.monitor.util.SpringBeanUtils;

@Service
public class SalukiMonitoWebService {

    private static final Logger      logger = LoggerFactory.getLogger(SalukiMonitorService.class);

    private final SalukiInvokeMapper mapper = SpringBeanUtils.getBean(SalukiInvokeMapper.class);

    /**
     * 统计调用数据用于图表展示
     *
     * @param invoke
     */
    public List<SalukiInvoke> countDubboInvoke(SalukiInvoke invoke) {
        if (StringUtils.isEmpty(invoke.getService())) {
            logger.error("统计查询缺少必要参数！");
            throw new RuntimeException("统计查询缺少必要参数！");
        }
        return mapper.countInvoke(invoke);
    }

    public List<String> getMethodsByService(SalukiInvoke invoke) {
        return mapper.getMethodsByService(invoke);
    }

    /**
     * 统计各方法调用信息
     *
     * @param invoke
     * @return
     */
    public List<SalukiInvoke> countDubboInvokeInfo(SalukiInvoke invoke) {
        if (StringUtils.isEmpty(invoke.getService()) || StringUtils.isEmpty(invoke.getMethod())) {
            logger.error("统计查询缺少必要参数！");
            throw new RuntimeException("统计查询缺少必要参数！");
        }
        return mapper.countInvokeInfo(invoke);
    }

    /**
     * 统计系统方法调用排序信息
     *
     * @param invoke
     * @return
     */
    public Map<String, List<SalukiInvoke>> countDubboInvokeTopTen(SalukiInvoke invoke) {
        Map<String, List<SalukiInvoke>> result = Maps.newHashMap();
        result.put("success", mapper.countInvokeSuccessTopTen(invoke));
        result.put("failure", mapper.countInvokeFailureTopTen(invoke));
        return result;
    }
}
