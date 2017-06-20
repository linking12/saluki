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
package com.quancheng.saluki.example.server;

import com.quancheng.examples.model.Hello;
import com.quancheng.examples.service.HelloServiceGrpc;
import com.quancheng.saluki.boot.SalukiService;

/**
 * @author liushiming
 * @version StubHellService.java, v 0.0.1 2017年6月20日 上午11:15:45 liushiming
 * @since JDK 1.8
 */
@SalukiService(service = "com.quancheng.examples.service.HelloService")
public class StubHellService extends HelloServiceGrpc.HelloServiceImplBase {

  @Override
  public void sayHello(com.quancheng.examples.model.Hello.HelloRequest request,
      io.grpc.stub.StreamObserver<com.quancheng.examples.model.Hello.HelloReply> responseObserver) {
    Hello.HelloReply.Builder replyBuild = Hello.HelloReply.newBuilder();
    replyBuild.setMessage(request.getName());
    responseObserver.onNext(replyBuild.build());
    responseObserver.onCompleted();
  }
}
