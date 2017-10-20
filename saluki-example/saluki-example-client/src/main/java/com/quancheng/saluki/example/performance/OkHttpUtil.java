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
package com.quancheng.saluki.example.performance;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author liushiming
 * @version OkHttpUtil.java, v 0.0.1 2017年10月20日 下午9:18:43 liushiming
 */
public class OkHttpUtil {
  private static OkHttpClient okHttpclient = null;

  static {
    okHttpclient = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).build();
  }

  public static String run(String url) throws Exception {
    Request request = new Request.Builder().url(url).build();
    Response response = okHttpclient.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IOException("Unexpected code " + response);
    }
    return response.body().string();

  }
}
