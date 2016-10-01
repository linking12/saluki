package com.quancheng.saluki.core.registry;

import java.util.List;

import com.quancheng.saluki.core.common.SalukiURL;

public interface NotifyListener {

    void notify(List<SalukiURL> urls);
}
