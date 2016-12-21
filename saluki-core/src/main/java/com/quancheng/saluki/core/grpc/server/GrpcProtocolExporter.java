/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.server;

import io.grpc.ServerServiceDefinition;

/**
 * @author shimingliu 2016年12月14日 下午10:08:24
 * @version GrpcProtocolExporter.java, v 0.0.1 2016年12月14日 下午10:08:24 shimingliu
 */
public interface GrpcProtocolExporter {

    public ServerServiceDefinition export(Class<?> protocol, Object protocolImpl);
}
