/*
 * Copyright 1999-2012 DianRong.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.saluki.core.grpc.client.hystrix;

import com.google.protobuf.Message;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.quancheng.saluki.core.common.GrpcURL;

import io.grpc.MethodDescriptor;

/**
 * @author liushiming 2017年4月26日 下午6:16:32
 * @version $Id: GrpcHystrixObservableCommand.java, v 0.0.1 2017年4月26日 下午6:16:32 liushiming
 */
public abstract class GrpcHystrixCommand extends HystrixCommand<Message> {

    private static final int DEFAULT_THREADPOOL_CORE_SIZE = 30;

    public GrpcHystrixCommand(GrpcURL refUrl, MethodDescriptor<Message, Message> methodDesc){
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(refUrl.getServiceInterface()))//
                    .andCommandKey(HystrixCommandKey.Factory.asKey(methodDesc.getFullMethodName()))//
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withCircuitBreakerRequestVolumeThreshold(20)// 10秒钟内至少19此请求失败，熔断器才发挥起作用
                                                                          .withCircuitBreakerSleepWindowInMilliseconds(30000)// 熔断器中断请求30秒后会进入半打开状态,放部分流量过去重试
                                                                          .withCircuitBreakerErrorThresholdPercentage(50)// 错误率达到50开启熔断保护
                                                                          .withExecutionTimeoutEnabled(false))// 禁用这里的超时
                    .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(getThreadPoolCoreSize(refUrl)))// 线程池为30
        );

    }

    private static int getThreadPoolCoreSize(GrpcURL refUrl) {
        return DEFAULT_THREADPOOL_CORE_SIZE;

    }
}
