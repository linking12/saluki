/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.saluki.core.grpc.cluster.io;

import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.Executor;

/**
 * A class for communicating that an operation has been or should be cancelled.
 *
 * @author sduskis
 * @version $Id: $Id
 */
public class CancellationToken {

  private final SettableFuture<Void> cancelledFuture = SettableFuture.create();

  /**
   * Add a listener that will be fired if and when this token is cancelled.
   *
   * @param runnable a {@link java.lang.Runnable} object.
   * @param executor a {@link java.util.concurrent.Executor} object.
   */
  public void addListener(Runnable runnable, Executor executor) {
    cancelledFuture.addListener(runnable, executor);
  }

  /**
   * Inform listeners that the action has been / should be cancelled.
   */
  public void cancel() {
    cancelledFuture.set(null);
  }
}
