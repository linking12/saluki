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
package com.quancheng.saluki.core.grpc.cluster.metrics;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * A timer metric which aggregates timing durations and provides duration statistics, plus
 * throughput statistics.
 */
public interface Timer {

  /**
   * A timing context.
   *
   * @see Timer#time()
   */
  interface Context extends Closeable {
    @Override
    /**
     * Stops the timer.
     */
    public void close();
  }

  /**
   * Returns a new {@link Context}.
   *
   * @return a new {@link Context}
   * @see Context
   */
  Context time();

  /**
   * Adds a recorded duration.
   *
   * @param duration the length of the duration
   * @param unit     the scale unit of {@code duration}
   */
  void update(long duration, TimeUnit unit);
}
