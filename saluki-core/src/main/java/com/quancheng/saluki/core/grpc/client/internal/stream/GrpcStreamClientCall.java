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
package com.quancheng.saluki.core.grpc.client.internal.stream;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.client.internal.GrpcCallOptions;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;

/**
 * @author liushiming
 * @version GrpcStreamClientCall.java, v 0.0.1 2017年8月16日 下午5:39:36 liushiming
 * @since JDK 1.8
 */
public interface GrpcStreamClientCall {

  public StreamObserver<Message> asyncClientStream(MethodDescriptor<Message, Message> method,
      StreamObserver<Message> responseObserver);

  public void asyncServerStream(MethodDescriptor<Message, Message> method,
      StreamObserver<Message> responseObserver, Message requestParam);

  public StreamObserver<Message> asyncBidiStream(MethodDescriptor<Message, Message> method,
      StreamObserver<Message> responseObserver);


  public static GrpcStreamClientCall create(final Channel channel, final GrpcURL refUrl) {
    CallOptions callOptions = GrpcCallOptions.createCallOptions(refUrl);
    return new GrpcStreamClientCall() {

      @Override
      public StreamObserver<Message> asyncClientStream(MethodDescriptor<Message, Message> method,
          StreamObserver<Message> responseObserver) {
        boolean streamingResponse = false;
        ClientCall<Message, Message> call = channel.newCall(method, callOptions);
        CallToStreamObserverAdapter<Message, Message> adapter =
            new CallToStreamObserverAdapter<Message, Message>(call);
        ClientCall.Listener<Message> responseListener =
            new StreamObserverToCallListenerAdapter<Message, Message>(responseObserver, adapter,
                streamingResponse);
        startCall(call, responseListener, streamingResponse);
        return adapter;
      }

      @Override
      public void asyncServerStream(MethodDescriptor<Message, Message> method,
          StreamObserver<Message> responseObserver, Message requestParam) {
        boolean streamingResponse = true;
        ClientCall<Message, Message> call = channel.newCall(method, callOptions);
        CallToStreamObserverAdapter<Message, Message> adapter =
            new CallToStreamObserverAdapter<Message, Message>(call);
        ClientCall.Listener<Message> responseListener =
            new StreamObserverToCallListenerAdapter<Message, Message>(responseObserver, adapter,
                streamingResponse);
        startCall(call, responseListener, streamingResponse);
        try {
          call.sendMessage(requestParam);
          call.halfClose();
        } catch (Throwable t) {
          call.cancel(null, t);
          throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
        }
      }

      @Override
      public StreamObserver<Message> asyncBidiStream(MethodDescriptor<Message, Message> method,
          StreamObserver<Message> responseObserver) {
        boolean streamingResponse = true;
        ClientCall<Message, Message> call = channel.newCall(method, callOptions);
        CallToStreamObserverAdapter<Message, Message> adapter =
            new CallToStreamObserverAdapter<Message, Message>(call);
        ClientCall.Listener<Message> responseListener =
            new StreamObserverToCallListenerAdapter<Message, Message>(responseObserver, adapter,
                streamingResponse);
        startCall(call, responseListener, streamingResponse);
        return adapter;
      }
    };

  }

  static void startCall(ClientCall<Message, Message> call,
      ClientCall.Listener<Message> responseListener, boolean streamingResponse) {
    call.start(responseListener, new Metadata());
    if (streamingResponse) {
      call.request(1);
    } else {
      call.request(2);
    }
  }


}
