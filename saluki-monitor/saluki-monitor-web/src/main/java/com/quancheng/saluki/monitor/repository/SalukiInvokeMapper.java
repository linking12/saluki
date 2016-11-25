package com.quancheng.saluki.monitor.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.quancheng.saluki.monitor.SalukiInvoke;

@Mapper
public interface SalukiInvokeMapper {

    void addInvoke(List<SalukiInvoke> invokes);

    List<SalukiInvoke> queryData(Map<String, String> paramter);

}
