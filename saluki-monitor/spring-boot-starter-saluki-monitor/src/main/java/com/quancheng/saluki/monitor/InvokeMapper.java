package com.quancheng.saluki.monitor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.quancheng.saluki.domain.GrpcInvoke;

@Mapper
public interface InvokeMapper {

    int addInvoke(GrpcInvoke invoke);

    int truncateTable();

    List<GrpcInvoke> queryData();

}
