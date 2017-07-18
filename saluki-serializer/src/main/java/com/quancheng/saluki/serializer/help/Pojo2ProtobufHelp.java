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
package com.quancheng.saluki.serializer.help;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.Message.Builder;
import com.quancheng.saluki.serializer.ProtobufAttribute;
import com.quancheng.saluki.serializer.ProtobufEntity;
import com.quancheng.saluki.serializer.ProtobufSerializer;
import com.quancheng.saluki.serializer.exception.ProtobufAnnotationException;
import com.quancheng.saluki.serializer.internal.ProtobufSerializerUtils;
import com.quancheng.saluki.serializer.utils.JException;
import com.quancheng.saluki.serializer.utils.JReflectionUtils;

/**
 * @author liushiming
 * @version Pojo2ProtobufHelp.java, v 0.0.1 2017年7月18日 下午1:49:19 liushiming
 * @since JDK 1.8
 */
public class Pojo2ProtobufHelp {

  private Pojo2ProtobufHelp() {

  }

  public static final Object getPojoFieldValue(Object pojo, ProtobufAttribute protobufAttribute,
      Field field) throws ProtobufAnnotationException {
    final String getter = protobufAttribute.pojoGetter();

    Object value = null;
    if (!getter.isEmpty()) {
      try {
        return JReflectionUtils.runMethod(pojo, getter);
      } catch (Exception e) {
        throw new ProtobufAnnotationException("Could not get a value for field " + field.getName()
            + " using configured getter of " + getter, e);
      }
    }

    try {
      value = JReflectionUtils.runGetter(pojo, field);
    } catch (Exception ee) {
      throw new ProtobufAnnotationException("Could not execute getter " + getter + " on class "
          + pojo.getClass().getCanonicalName() + ": " + ee, ee);
    }

    if (value == null && protobufAttribute.required()) {
      throw new ProtobufAnnotationException("Required field " + field.getName() + " on class "
          + pojo.getClass().getCanonicalName() + " is null");
    }

    return value;
  }

  public static final Object serializeToProtobufEntity(Object pojo) throws JException {
    final ProtobufEntity protoBufEntity =
        ProtobufSerializerUtils.getProtobufEntity(pojo.getClass());
    if (protoBufEntity == null) {
      return pojo;
    }
    return new ProtobufSerializer().toProtobuf(pojo);
  }

  public static final void setProtobufFieldValue(ProtobufAttribute protobufAttribute,
      Builder protoObjBuilder, String setter, Object fieldValue) throws NoSuchMethodException,
      SecurityException, ProtobufAnnotationException, InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Class<? extends Object> fieldValueClass = fieldValue.getClass();
    Class<? extends Object> gpbClass = fieldValueClass;
    // Need to convert the argument class from non-primitives to primitives, as Protobuf uses these.
    gpbClass = ProtobufSerializerUtils.getProtobufClass(fieldValue, gpbClass);
    final Method gpbMethod = protoObjBuilder.getClass().getDeclaredMethod(setter, gpbClass);
    gpbMethod.invoke(protoObjBuilder, fieldValue);
  }

  /**
   * 集合元素转化为Protobuf
   */
  public static final Object convertCollectionToProtobufs(
      Collection<Object> collectionOfNonProtobufs) throws JException {
    if (collectionOfNonProtobufs.isEmpty()) {
      return collectionOfNonProtobufs;
    }
    final Object first = collectionOfNonProtobufs.toArray()[0];
    if (!ProtobufSerializerUtils.isProtbufEntity(first)) {
      return collectionOfNonProtobufs;
    }
    final Collection<Object> newCollectionValues;
    if (collectionOfNonProtobufs instanceof Set) {
      newCollectionValues = new HashSet<>();
    } else {
      newCollectionValues = new ArrayList<>();
    }
    for (Object iProtobufGenObj : collectionOfNonProtobufs) {
      newCollectionValues.add(Pojo2ProtobufHelp.serializeToProtobufEntity(iProtobufGenObj));
    }
    return newCollectionValues;
  }

  /**
   * Map元素转化为Protobuf
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static final Object convertMapToProtobufs(Map<?, ?> mapOfNonProtobufs) throws JException {
    if (mapOfNonProtobufs.isEmpty()) {
      return mapOfNonProtobufs;
    }
    final Object keyFirst = mapOfNonProtobufs.keySet().toArray()[0];
    final Object valueFirst = mapOfNonProtobufs.get(keyFirst);
    if (!ProtobufSerializerUtils.isProtbufEntity(keyFirst)
        && !ProtobufSerializerUtils.isProtbufEntity(valueFirst)) {
      return mapOfNonProtobufs;
    }
    final Map newMapValues = new HashMap<>();
    for (Map.Entry<?, ?> entry : mapOfNonProtobufs.entrySet()) {
      Object newMapValuesKey = Pojo2ProtobufHelp.serializeToProtobufEntity(entry.getKey());
      Object newMapValuesValue = Pojo2ProtobufHelp.serializeToProtobufEntity(entry.getValue());
      newMapValues.put(newMapValuesKey, newMapValuesValue);
    }
    return newMapValues;
  }

}
