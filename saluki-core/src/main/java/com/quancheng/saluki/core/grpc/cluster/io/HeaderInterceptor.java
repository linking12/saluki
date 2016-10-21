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

import io.grpc.Metadata;

/**
 * This interface provides a simple mechanism to update headers before a gRPC method is called.
 *
 * @author sduskis
 * @version $Id: $Id
 */
public interface HeaderInterceptor {
  /**
   * Modify the headers before an RPC call is made.
   *
   * @param headers a {@link io.grpc.Metadata} object.
   * @throws java.lang.Exception if any.
   */
  void updateHeaders(Metadata headers) throws Exception;
}
