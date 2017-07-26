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
package com.quancheng.saluki.core.grpc.client.failover;

import com.google.common.util.concurrent.AbstractFuture;

/**
 * @author liushiming
 * @version CompletionFuture.java, v 0.0.1 2017年7月14日 下午9:42:42 liushiming
 * @since JDK 1.8
 */
public class CompletionFuture<T> extends AbstractFuture<T> {


  @Override
  protected boolean set(T resp) {
    return super.set(resp);
  }

  @Override
  protected boolean setException(Throwable throwable) {
    return super.setException(throwable);
  }

}
