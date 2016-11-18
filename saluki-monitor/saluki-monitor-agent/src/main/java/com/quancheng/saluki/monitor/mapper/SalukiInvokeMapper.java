package com.quancheng.saluki.monitor.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.quancheng.saluki.monitor.domain.SalukiInvokeStatistics;

@Mapper
public interface SalukiInvokeMapper {

    int addInvoke(SalukiInvokeStatistics invoke);

    int truncateTable();

    List<SalukiInvokeStatistics> queryDataByService(String service);

}
