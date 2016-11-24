package com.quancheng.saluki.monitor.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.quancheng.saluki.monitor.SalukiInvoke;

@Mapper
public interface SalukiInvokeMapper {

    int addInvoke(List<SalukiInvoke> invokes);

    List<SalukiInvoke> queryDataBySingleMachine(String service, String type, String ip);

    List<SalukiInvoke> querySumDataByService(String service, String type);
}
