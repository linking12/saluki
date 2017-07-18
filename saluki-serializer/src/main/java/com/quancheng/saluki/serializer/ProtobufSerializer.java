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
package com.quancheng.saluki.serializer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.quancheng.saluki.serializer.exception.ProtobufAnnotationException;
import com.quancheng.saluki.serializer.exception.ProtobufException;
import com.quancheng.saluki.serializer.help.Pojo2ProtobufHelp;
import com.quancheng.saluki.serializer.help.Protobuf2PojoHelp;
import com.quancheng.saluki.serializer.internal.ProtobufSerializerUtils;
import com.quancheng.saluki.serializer.utils.JReflectionUtils;


/**
 * @author liushiming
 * @version ProtobufSerializer2.java, v 0.0.1 2017年7月18日 下午12:49:24 liushiming
 * @since JDK 1.8
 */
public class ProtobufSerializer implements IProtobufSerializer {

  /**
   * @see com.quancheng.saluki.serializer.IProtobufSerializer#toProtobuf(java.lang.Object)
   */
  @Override
  @SuppressWarnings({"unchecked", "rawtypes", "unused"})
  public Message toProtobuf(Object pojo) throws ProtobufException {
    try {
      final Class<?> fromClazz = (Class<?>) pojo.getClass();
      final Class<? extends GeneratedMessageV3> protoClazz =
          ProtobufSerializerUtils.getProtobufClassFromPojoAnno(fromClazz);
      if (protoClazz == null) {
        throw new ProtobufAnnotationException(
            "Doesn't seem like " + fromClazz + " is ProtobufEntity");
      }
      final Map<Field, ProtobufAttribute> protoBufFields =
          ProtobufSerializerUtils.getAllProtbufFields(fromClazz);
      if (protoBufFields.isEmpty()) {
        return null;
      }
      final Method newBuilderMethod = protoClazz.getMethod("newBuilder");
      final Builder protoObjBuilder = (Builder) newBuilderMethod.invoke(null);
      for (Entry<Field, ProtobufAttribute> entry : protoBufFields.entrySet()) {
        final Field field = entry.getKey();
        final ProtobufAttribute gpbAnnotation = entry.getValue();
        final String fieldName = field.getName();
        // 1. Determine validity of value
        Object value = Pojo2ProtobufHelp.getPojoFieldValue(pojo, gpbAnnotation, field);
        // If value is null and it is not required, skip, as the default for Protobuf values is null
        if (value == null) {
          continue;
        }
        // 2. Call recursively if this is a ProtobufEntity
        value = Pojo2ProtobufHelp.serializeToProtobufEntity(value);
        // 3. Special recursively if this is a ProtobufEntity
        if (value instanceof Collection) {
          value = Pojo2ProtobufHelp.convertCollectionToProtobufs((Collection<Object>) value);
          if (((Collection) value).isEmpty()) {
            continue;
          }
        }
        if (value instanceof Map) {
          value = Pojo2ProtobufHelp.convertMapToProtobufs((Map) value);
          if (((Map) value).isEmpty()) {
            continue;
          }
        }
        String setter = ProtobufSerializerUtils.getProtobufSetter(gpbAnnotation, field, value);
        if (value instanceof Enum) {
          value = JReflectionUtils.runMethod(value, "getNumber");
          setter = setter + "Value";
        }
        Pojo2ProtobufHelp.setProtobufFieldValue(gpbAnnotation, protoObjBuilder, setter, value);
      }
      return protoObjBuilder.build();
    } catch (Exception e) {
      throw new ProtobufException(
          "Could not generate Protobuf object for " + pojo.getClass() + ": " + e, e);
    }
  }

  /**
   * @see com.quancheng.saluki.serializer.IProtobufSerializer#fromProtobuf(com.google.protobuf.Message,
   *      java.lang.Class)
   */
  @Override
  public Object fromProtobuf(Message protobuf, Class<?> pojoClazz) throws ProtobufException {
    try {
      final Class<? extends Message> protoClazz =
          ProtobufSerializerUtils.getProtobufClassFromPojoAnno(pojoClazz);
      if (protoClazz == null) {
        throw new ProtobufAnnotationException(
            "Doesn't seem like " + pojoClazz + " is ProtobufEntity");
      }
      final Map<Field, ProtobufAttribute> protobufFields =
          ProtobufSerializerUtils.getAllProtbufFields(pojoClazz);
      if (protobufFields.isEmpty()) {
        throw new ProtobufException("No protoBuf fields have been annotated on the class "
            + pojoClazz + ", thus cannot continue.");
      }
      Object pojo = pojoClazz.newInstance();
      for (Entry<Field, ProtobufAttribute> entry : protobufFields.entrySet()) {
        final Field field = entry.getKey();
        final ProtobufAttribute protobufAttribute = entry.getValue();
        final String setter = ProtobufSerializerUtils.getPojoSetter(protobufAttribute, field);
        Object protobufValue =
            Protobuf2PojoHelp.getProtobufFieldValue(protobuf, protobufAttribute, field);
        if (protobufValue == null) {
          continue;
        }
        Protobuf2PojoHelp.setPojoFieldValue(pojo, setter, protobufValue, protobufAttribute);
      }
      return pojo;
    } catch (Exception e) {
      throw new ProtobufException("Could not generate POJO of type " + pojoClazz
          + " from Protobuf object " + protobuf.getClass() + ": " + e, e);
    }
  }



}
