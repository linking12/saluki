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
package com.quancheng.saluki;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.quancheng.plugin.common.CommandProtoc;

/**
 * @author liushiming
 * @version Validator.java, v 0.0.1 2017年7月3日 上午10:03:00 liushiming
 * @since JDK 1.8
 */
public class Validator {

  /**
   * 
   * @author liushiming
   * @param args
   * @since JDK 1.8
   */
  public static void main(String[] args) {
    CommandProtoc commondProtoc = CommandProtoc.configProtoPath(
        "/Users/liushiming/project/java/saluki/saluki-plugin/saluki-plugin-common/src/test/java/com/quancheng/saluki");

    FileDescriptorSet fileDescriptorSet = commondProtoc.invoke(
        "/Users/liushiming/project/java/saluki/saluki-plugin/saluki-plugin-common/src/test/java/com/quancheng/saluki/saluki_service.proto");
    System.out.println(fileDescriptorSet);
  }

}
