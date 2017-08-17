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
package com.quancheng.saluki.core.grpc.client.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.GrpcURL;

import io.grpc.CallOptions;

/**
 * @author liushiming
 * @version GrpcClientCall.java, v 0.0.1 2017年8月16日 下午6:20:23 liushiming
 * @since JDK 1.8
 */
public abstract class GrpcCallOptions {
  public static final CallOptions.Key<ConcurrentHashMap<String, Object>> CALLOPTIONS_CUSTOME_KEY =
      CallOptions.Key.of("custom_options", new ConcurrentHashMap<String, Object>());

  public static final String GRPC_REF_URL = "grpc-refurl";

  public static final String GRPC_CURRENT_ADDR_KEY = "current-address";

  public static final String GRPC_NAMERESOVER_ATTRIBUTES = "nameresolver-attributes";

  private static final Map<String, CallOptions> CACHEOPTIONS_CACHE = Maps.newConcurrentMap();

  public static CallOptions createCallOptions(final GrpcURL refUrl) {
    String serviceName = refUrl.getServiceInterface();
    CallOptions options = CACHEOPTIONS_CACHE.get(serviceName);
    if (options == null) {
      ConcurrentHashMap<String, Object> customOptions = new ConcurrentHashMap<String, Object>();
      customOptions.put(GRPC_REF_URL, refUrl);
      options = CallOptions.DEFAULT.withOption(CALLOPTIONS_CUSTOME_KEY, customOptions);
      CACHEOPTIONS_CACHE.put(serviceName, options);
      return options;
    } else {
      return options;
    }
  }

  public static Map<String, Object> getAffinity(final GrpcURL refUrl) {
    String serviceName = refUrl.getServiceInterface();
    return CACHEOPTIONS_CACHE.get(serviceName).getOption(CALLOPTIONS_CUSTOME_KEY);
  }


}
