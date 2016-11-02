package com.quancheng.saluki.monitor.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.quancheng.saluki.monitor.domain.DubboInvoke;

@Mapper
public interface DubboInvokeMapper {

    int addDubboInvoke(DubboInvoke dubboInvoke);

}
