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
package com.quancheng.saluki.gateway.grpc.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


/**
 * @author liushiming
 * @version ApiClassCache.java, v 0.0.1 2017年8月7日 上午8:43:13 liushiming
 * @since JDK 1.8
 */
public class ApiClassCache {


  private static final Cache<String, Class<?>> APICLASSCACHE = CacheBuilder.newBuilder() //
      .concurrencyLevel(8) //
      .weakKeys()//
      .recordStats() //
      .build();

  private static class LazyHolder {

    private static final ApiClassCache INSTANCE = new ApiClassCache();
  }

  private ApiClassCache() {}

  public static final ApiClassCache getInstance() {
    return LazyHolder.INSTANCE;
  }


  public void put(String api, Class<?> apiClass) {
    APICLASSCACHE.put(api, apiClass);
  }

  public Class<?> get(String api) {
    try {
      return APICLASSCACHE.get(api, new Callable<Class<?>>() {

        @Override
        public Class<?> call() throws Exception {
          return null;
        }
      });
    } catch (ExecutionException e) {
      return null;
    }
  }

  public void invalidate() {
    APICLASSCACHE.invalidateAll();
  }

}
