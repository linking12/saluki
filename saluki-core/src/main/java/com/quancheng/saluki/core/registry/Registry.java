package com.quancheng.saluki.core.registry;

import java.util.List;

import com.quancheng.saluki.core.common.SalukiURL;

public interface Registry {

    /**
     * 注册服务
     */
    void register(SalukiURL url);

    /**
     * 取消注册
     */
    void unregister(SalukiURL url);

    /**
     * 订阅服务
     */
    void subscribe(SalukiURL url, NotifyListener listener);

    /**
     * 取消订阅
     */
    void unsubscribe(SalukiURL url, NotifyListener listener);

    /**
     * 查询服务
     */
    List<SalukiURL> discover(SalukiURL url);

}
