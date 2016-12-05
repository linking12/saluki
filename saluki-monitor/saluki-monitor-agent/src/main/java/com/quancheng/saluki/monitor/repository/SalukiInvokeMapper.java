package com.quancheng.saluki.monitor.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.quancheng.saluki.monitor.SalukiInvoke;

@Mapper
public interface SalukiInvokeMapper {

    int addInvoke(SalukiInvoke invoke);

    int truncateTable();

    List<SalukiInvoke> queryData();

}
