/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client;

import com.quancheng.saluki.core.common.GrpcURL;

import io.grpc.Channel;

/**
 * @author shimingliu 2016年12月14日 下午5:50:42
 * @version GrpcProtocolClient.java, v 0.0.1 2016年12月14日 下午5:50:42 shimingliu
 */
public interface GrpcProtocolClient<T> {

    public T getGrpcClient(ChannelPool channelCall, int callType, int callTimeout);

    public interface ChannelPool {

        public Channel borrowChannel(final GrpcURL refUrl);

        public void returnChannel(final GrpcURL refUrl, final Channel channel);
    }

}
