/*
 * Copyright 1999-2012 DianRong.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.quancheng.saluki.core.grpc.client.hystrix;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.client.failover.GrpcClientCall;
import com.quancheng.saluki.core.grpc.exception.RpcErrorMsgConstant;
import com.quancheng.saluki.core.grpc.exception.RpcServiceException;

import io.grpc.MethodDescriptor;

/**
 * @author liushiming 2017年4月26日 下午5:21:49
 * @version $Id: UnaryCommand.java, v 0.0.1 2017年4月26日 下午5:21:49 liushiming
 */
public class GrpcFutureUnaryCommand extends GrpcHystrixCommand {

  private static final Logger logger = LoggerFactory.getLogger(GrpcHystrixCommand.class);

  public GrpcFutureUnaryCommand(String serviceName, String methodName, Boolean isEnabledFallBack) {
    super(serviceName, methodName, isEnabledFallBack);
  }

  /**
   * @see com.quancheng.saluki.core.grpc.client.hystrix.GrpcHystrixCommand#run0(com.google.protobuf.Message,
   *      io.grpc.MethodDescriptor, java.lang.Integer,
   *      com.quancheng.saluki.core.grpc.client.failover.GrpcClientCall)
   */
  @Override
  protected Message run0(Message req, MethodDescriptor<Message, Message> methodDesc,
      Integer timeOut, GrpcClientCall clientCall) {
    try {
      return clientCall.unaryFuture(req, methodDesc).get(timeOut, TimeUnit.MILLISECONDS);
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);
      super.cacheCurrentServer();
      if (e instanceof TimeoutException) {
        RpcServiceException rpcService =
            new RpcServiceException(e, RpcErrorMsgConstant.SERVICE_TIMEOUT);
        throw rpcService;
      } else {
        RpcServiceException rpcService =
            new RpcServiceException(e, RpcErrorMsgConstant.BIZ_DEFAULT_EXCEPTION);
        throw rpcService;
      }
    }
  }

}
