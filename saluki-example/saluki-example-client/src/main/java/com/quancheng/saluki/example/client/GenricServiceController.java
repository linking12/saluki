/*
 * Copyright 2014-2017 the original author or authors.
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
package com.quancheng.saluki.example.client;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.saluki.example.model.First;
import com.saluki.example.model.Second;

/**
 * @author liushiming
 * @version GenricServiceController.java, v 0.0.1 2017年7月15日 上午11:09:11 liushiming
 * @since JDK 1.8
 */
@RestController
@RequestMapping("/genric")
public class GenricServiceController {

  @SalukiReference
  private GenericService genricService;

  @RequestMapping("/hello")
  public HelloReply view(@RequestParam(value = "name", required = false) String name) {
    String serviceName = "com.quancheng.examples.service.HelloService";
    String method = "sayHello";
    String[] parameterTypes = new String[] {"com.quancheng.examples.model.hello.HelloRequest",
        "com.quancheng.examples.model.hello.HelloReply"};
    HelloRequest request = new HelloRequest();
    request.setName(name);
    Object[] args = new Object[] {request};
    HelloReply reply = (HelloReply) genricService.$invoke(serviceName, "example", "1.0.0", method,
        parameterTypes, args);
    return reply;
  }


  @RequestMapping("/hello_1")
  public HelloReply view1(@RequestParam(value = "name", required = false) String name) {
    String serviceName = "com.quancheng.examples.service.HelloService";
    String method = "sayHello";
    String[] parameterTypes = new String[] {"com.quancheng.examples.model.hello.HelloRequest",
        "com.quancheng.examples.model.hello.HelloReply"};
    HelloRequest request = new HelloRequest();
    request.setName(name);
    Object[] args = new Object[] {request};
    RpcContext.getContext().setHoldenGroups(new HashSet<>(Arrays.asList(First.class)));
    HelloReply reply = (HelloReply) genricService.$invoke(serviceName, "example", "1.0.0", method,
        parameterTypes, args);
    return reply;
  }

  @RequestMapping("/hello_2")
  public HelloReply view2(@RequestParam(value = "name", required = false) String name) {
    String serviceName = "com.quancheng.examples.service.HelloService";
    String method = "sayHello";
    String[] parameterTypes = new String[] {"com.quancheng.examples.model.hello.HelloRequest",
        "com.quancheng.examples.model.hello.HelloReply"};
    HelloRequest request = new HelloRequest();
    request.setName(name);
    Object[] args = new Object[] {request};
    RpcContext.getContext().setHoldenGroups(new HashSet<>(Arrays.asList(Second.class)));
    HelloReply reply = (HelloReply) genricService.$invoke(serviceName, "example", "1.0.0", method,
        parameterTypes, args);
    return reply;
  }
}
