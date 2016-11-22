package com.quancheng.saluki.monitor.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.quancheng.saluki.monitor.SalukiInvoke;
import com.quancheng.saluki.monitor.SalukiInvokeStatistics;

@Mapper
public interface SalukiInvokeMapper {

    int addInvoke(SalukiInvoke invoke);

    int truncateTable();

    List<SalukiInvoke> queryData(Map<String, String> queryType);

    List<SalukiInvokeStatistics> queryStatistics(Map<String, String> queryType);

}
