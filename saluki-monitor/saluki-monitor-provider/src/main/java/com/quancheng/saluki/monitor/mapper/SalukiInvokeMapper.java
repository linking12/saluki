package com.quancheng.saluki.monitor.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.quancheng.saluki.monitor.domain.SalukiInvoke;

@Mapper
public interface SalukiInvokeMapper {

    int addInvoke(SalukiInvoke dubboInvoke);

    int truncateTable();

}
