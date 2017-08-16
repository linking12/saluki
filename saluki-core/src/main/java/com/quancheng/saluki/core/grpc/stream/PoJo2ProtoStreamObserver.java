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
package com.quancheng.saluki.core.grpc.stream;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.util.SerializerUtil;
import com.quancheng.saluki.serializer.exception.ProtobufException;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.netty.util.internal.ThrowableUtil;

/**
 * @author liushiming
 * @version ServerCallStreamObserverWrap.java, v 0.0.1 2017年8月15日 上午11:30:58 liushiming
 * @since JDK 1.8
 */
public class PoJo2ProtoStreamObserver implements StreamObserver<Object> {

  private final StreamObserver<Message> streamObserver;

  private PoJo2ProtoStreamObserver(StreamObserver<Message> streamObserver) {
    this.streamObserver = streamObserver;
  }

  public static PoJo2ProtoStreamObserver newObserverWrap(StreamObserver<Message> streamObserver) {
    return new PoJo2ProtoStreamObserver(streamObserver);
  }

  @Override
  public void onNext(Object value) {
    try {
      Object respPojo = value;
      Message respProtoBufer = SerializerUtil.pojo2Protobuf(respPojo);
      streamObserver.onNext(respProtoBufer);
    } catch (ProtobufException e) {
      String stackTrace = ThrowableUtil.stackTraceToString(e);
      StatusRuntimeException statusException =
          Status.UNAVAILABLE.withDescription(stackTrace).asRuntimeException();
      streamObserver.onError(statusException);
    }
  }


  @Override
  public void onError(Throwable t) {
    streamObserver.onError(t);;
  }


  @Override
  public void onCompleted() {
    streamObserver.onCompleted();
  }


}
