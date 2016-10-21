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

import java.util.concurrent.TimeUnit;

/**
 * A registry of metric instances.
 */
public interface MetricRegistry {
  /** Creates a named {@link Counter}. */
  Counter counter(String name);

  /** Creates a named {@link Timer}. */
  Timer timer(String name);

  /** Creates a named {@link Meter}. */
  Meter meter(String name);

  /**
   * An implementation of {@link MetricRegistry} that doesn't actually track metrics, but doesn't
   * throw a NullPointerException.
   */
  public static final MetricRegistry NULL_METRICS_REGISTRY =
      new MetricRegistry() {

        private final Counter NULL_COUNTER =
            new Counter() {
              @Override
              public void inc() {}

              @Override
              public void dec() {}
            };

        private final Timer NULL_TIMER =
            new Timer() {
              private Context NULL_CONTEXT =
                  new Context() {
                    @Override
                    public void close() {}
                  };

              @Override
              public Context time() {
                return NULL_CONTEXT;
              }

              @Override
              public void update(long duration, TimeUnit unit) {
                // ignore.
              }
            };

        private final Meter NULL_METER =
            new Meter() {
              @Override
              public void mark() {}

              @Override
              public void mark(long size) {}
            };

        @Override
        public Timer timer(String name) {
          return NULL_TIMER;
        }

        @Override
        public Meter meter(String name) {
          return NULL_METER;
        }

        @Override
        public Counter counter(String name) {
          return NULL_COUNTER;
        }
      };
}
