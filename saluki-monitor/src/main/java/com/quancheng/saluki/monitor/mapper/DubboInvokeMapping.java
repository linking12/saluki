package com.quancheng.saluki.monitor.mapper;

import com.quancheng.saluki.monitor.domain.DubboInvoke;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface DubboInvokeMapping extends Mapper<DubboInvoke>, MySqlMapper<DubboInvoke> {

}
