package com.quancheng.saluki.monitor.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.quancheng.saluki.monitor.domain.SalukiInvoke;

@Mapper
public interface SalukiInvokeMapper {

    int addInvoke(SalukiInvoke invoke);

    int truncateTable();

    List<SalukiInvoke> queryAllInvoke(String service);

    List<SalukiInvoke> countInvoke(SalukiInvoke invoke);

    List<SalukiInvoke> countInvokeInfo(SalukiInvoke invoke);

    List<String> getMethodsByService(SalukiInvoke invoke);

    List<SalukiInvoke> countInvokeSuccessTopTen(SalukiInvoke invoke);

    List<SalukiInvoke> countInvokeFailureTopTen(SalukiInvoke invoke);

    List<SalukiInvoke> queryAllInvoke(SalukiInvoke invoke);
}
