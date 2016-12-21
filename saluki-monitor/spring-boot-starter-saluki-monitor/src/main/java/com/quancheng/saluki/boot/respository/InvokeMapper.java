package com.quancheng.saluki.boot.respository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.quancheng.saluki.boot.domain.GrpcInvoke;

@Mapper
public interface InvokeMapper {

    int addInvoke(GrpcInvoke invoke);

    int truncateTable();

    List<GrpcInvoke> queryData();

}
