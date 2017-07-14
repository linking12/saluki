/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client;

import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.client.internal.DefaultProxyClient;
import com.quancheng.saluki.core.grpc.client.internal.GenericProxyClient;
import com.quancheng.saluki.core.grpc.client.internal.GrpcStubClient;
import com.quancheng.saluki.core.utils.ReflectUtils;

import io.grpc.stub.AbstractStub;

/**
 * @author shimingliu 2016年12月14日 下午5:49:51
 * @version GrpcClientStrategy.java, v 0.0.1 2016年12月14日 下午5:49:51 shimingliu
 */
public class GrpcClientStrategy {

  private final GrpcProtocolClient<Object> grpcClient;

  private final GrpcProtocolClient.ChannelCall call;

  private final int callType;

  private final int callTimeout;

  public GrpcClientStrategy(GrpcURL refUrl, GrpcProtocolClient.ChannelCall call) {
    this.call = call;
    this.callType = refUrl.getParameter(Constants.ASYNC_KEY, Constants.RPCTYPE_ASYNC);
    this.callTimeout = refUrl.getParameter(Constants.TIMEOUT, Constants.RPC_ASYNC_DEFAULT_TIMEOUT);
    this.grpcClient = buildProtoClient(refUrl);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private GrpcProtocolClient<Object> buildProtoClient(GrpcURL refUrl) {
    boolean isGeneric = refUrl.getParameter(Constants.GENERIC_KEY, Boolean.FALSE);
    boolean isGrpcStub = refUrl.getParameter(Constants.GRPC_STUB_KEY, Boolean.FALSE);
    if (isGeneric) {
      return new GenericProxyClient<Object>(refUrl);
    } else {
      if (isGrpcStub) {
        String stubClassName = refUrl.getParameter(Constants.INTERFACECLASS_KEY);
        try {
          Class<? extends AbstractStub> stubClass =
              (Class<? extends AbstractStub>) ReflectUtils.name2class(stubClassName);
          return new GrpcStubClient<Object>(stubClass, refUrl);
        } catch (ClassNotFoundException e) {
          throw new IllegalArgumentException("grpc stub client the class must exist in classpath",
              e);
        }
      } else {
        return new DefaultProxyClient<Object>(refUrl);
      }
    }
  }

  public Object getGrpcClient() {
    return grpcClient.getGrpcClient(call, callType, callTimeout);
  }
}
