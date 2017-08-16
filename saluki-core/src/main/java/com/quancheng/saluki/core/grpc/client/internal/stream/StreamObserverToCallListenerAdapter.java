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

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

/**
 * @author liushiming
 * @version StreamObserverToCallListenerAdapter.java, v 0.0.1 2017年8月16日 下午5:31:23 liushiming
 * @since JDK 1.8
 */
public class StreamObserverToCallListenerAdapter<Request, Response>
    extends ClientCall.Listener<Response> {
  private final StreamObserver<Response> observer;
  private final CallToStreamObserverAdapter<Request, Response> adapter;
  private final boolean streamingResponse;
  private boolean firstResponseReceived;

  StreamObserverToCallListenerAdapter(StreamObserver<Response> observer,
      CallToStreamObserverAdapter<Request, Response> adapter, boolean streamingResponse) {
    this.observer = observer;
    this.streamingResponse = streamingResponse;
    this.adapter = adapter;
    if (observer instanceof ClientResponseObserver) {
      @SuppressWarnings("unchecked")
      ClientResponseObserver<Request, Response> clientResponseObserver =
          (ClientResponseObserver<Request, Response>) observer;
      clientResponseObserver.beforeStart(adapter);
    }
    adapter.freeze();
  }

  @Override
  public void onHeaders(Metadata headers) {}

  @Override
  public void onMessage(Response message) {
    if (firstResponseReceived && !streamingResponse) {
      throw Status.INTERNAL
          .withDescription("More than one responses received for unary or client-streaming call")
          .asRuntimeException();
    }
    firstResponseReceived = true;
    observer.onNext(message);
    if (streamingResponse && adapter.isAutoFlowControlEnabled()) {
      adapter.request(1);
    }
  }

  @Override
  public void onClose(Status status, Metadata trailers) {
    if (status.isOk()) {
      observer.onCompleted();
    } else {
      observer.onError(status.asRuntimeException(trailers));
    }
  }

  @Override
  public void onReady() {
    Runnable runnable = adapter.getOnReadyHandler();
    if (runnable != null) {
      runnable.run();
    }
  }
}
