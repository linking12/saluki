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

import javax.annotation.Nullable;

import io.grpc.ClientCall;
import io.grpc.stub.ClientCallStreamObserver;

/**
 * @author liushiming
 * @version CallToStreamObserverAdapter.java, v 0.0.1 2017年8月16日 下午5:28:24 liushiming
 * @since JDK 1.8
 */
public class CallToStreamObserverAdapter<Request, Response>
    extends ClientCallStreamObserver<Request> {

  private boolean frozen;
  private final ClientCall<Request, ?> call;
  private Runnable onReadyHandler;
  private boolean autoFlowControlEnabled = true;

  public Runnable getOnReadyHandler() {
    return onReadyHandler;
  }

  public boolean isAutoFlowControlEnabled() {
    return autoFlowControlEnabled;
  }

  public CallToStreamObserverAdapter(ClientCall<Request, ?> call) {
    this.call = call;
  }

  public void freeze() {
    this.frozen = true;
  }

  @Override
  public void onNext(Request value) {
    call.sendMessage(value);
  }

  @Override
  public void onError(Throwable t) {
    call.cancel("Cancelled by client with StreamObserver.onError()", t);
  }

  @Override
  public void onCompleted() {
    call.halfClose();
  }

  @Override
  public boolean isReady() {
    return call.isReady();
  }

  @Override
  public void setOnReadyHandler(Runnable onReadyHandler) {
    if (frozen) {
      throw new IllegalStateException("Cannot alter onReadyHandler after call started");
    }
    this.onReadyHandler = onReadyHandler;
  }

  @Override
  public void disableAutoInboundFlowControl() {
    if (frozen) {
      throw new IllegalStateException("Cannot disable auto flow control call started");
    }
    autoFlowControlEnabled = false;
  }

  @Override
  public void request(int count) {
    call.request(count);
  }

  @Override
  public void setMessageCompression(boolean enable) {
    call.setMessageCompression(enable);
  }

  @Override
  public void cancel(@Nullable String message, @Nullable Throwable cause) {
    call.cancel(message, cause);
  }


}
