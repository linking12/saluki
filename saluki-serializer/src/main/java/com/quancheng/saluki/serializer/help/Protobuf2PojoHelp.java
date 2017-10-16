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
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import com.quancheng.saluki.serializer.ProtobufAttribute;
import com.quancheng.saluki.serializer.ProtobufEntity;
import com.quancheng.saluki.serializer.ProtobufSerializer;
import com.quancheng.saluki.serializer.exception.ProtobufException;
import com.quancheng.saluki.serializer.internal.ProtobufSerializerUtils;
import com.quancheng.saluki.serializer.utils.JException;
import com.quancheng.saluki.serializer.utils.JReflectionUtils;

/**
 * @author liushiming
 * @version Protobuf2PojoHelp.java, v 0.0.1 2017年7月18日 下午1:54:59 liushiming
 * @since JDK 1.8
 */
public class Protobuf2PojoHelp {

  private Protobuf2PojoHelp() {

  }

  public static final Object serializeFromProtobufEntity(Message protoBuf, Class<?> pojoClazz)
      throws JException {
    final ProtobufEntity protoBufEntity = ProtobufSerializerUtils.getProtobufEntity(pojoClazz);
    if (protoBufEntity == null) {
      return protoBuf;
    }
    return new ProtobufSerializer().fromProtobuf(protoBuf, pojoClazz);
  }

  @SuppressWarnings("rawtypes")
  public static final Object getProtobufFieldValue(Message protoBuf,
      ProtobufAttribute protobufAttribute, Field field)
      throws JException, InstantiationException, IllegalAccessException {
    final String getter = ProtobufSerializerUtils.getProtobufGetter(protobufAttribute, field);
    // This is used to determine if the Protobuf message has populated this value
    Boolean isCollection = Boolean.FALSE;
    if (Collection.class.isAssignableFrom(field.getType())) {
      isCollection = Boolean.TRUE;
    }
    // Go ahead and fun the getter
    Object protobufValue = JReflectionUtils.runMethod(protoBuf, getter, (Object[]) null);
    if (isCollection && ((Collection) protobufValue).isEmpty()) {
      return null;
    }
    // If the field itself is a ProtbufEntity, serialize that!
    if (protobufValue instanceof Message
        && ProtobufSerializerUtils.isProtbufEntity(field.getType())) {
      protobufValue = serializeFromProtobufEntity((Message) protobufValue, field.getType());
    }
    if (protobufValue instanceof Collection) {
      protobufValue = convertCollectionFromProtobufs(field, (Collection<?>) protobufValue);
      if (((Collection) protobufValue).isEmpty()) {
        return null;
      }
    }
    if (protobufValue instanceof ProtocolMessageEnum) {
      protobufValue = JReflectionUtils.runStaticMethod(field.getType(), "forNumber",
          ((ProtocolMessageEnum) protobufValue).getNumber());
    }
    return protobufValue;
  }

  public static final void setPojoFieldValue(Object pojo, String setter, Object protobufValue,
      ProtobufAttribute protobufAttribute, Field field)
      throws InstantiationException, IllegalAccessException, JException {
    Class<? extends Object> argClazz = null;
    if (protobufValue instanceof List) {
      final ArrayList<Object> newCollectionValues = new ArrayList<>();
      newCollectionValues.addAll((Collection<?>) protobufValue);
      protobufValue = newCollectionValues;
      argClazz = ArrayList.class;
    } else if (protobufValue instanceof Map) {
      final Map<Object, Object> newMapValues = new HashMap<>();
      newMapValues.putAll((Map<?, ?>) protobufValue);
      protobufValue = newMapValues;
      argClazz = Map.class;
    } else if (protobufValue instanceof ProtocolMessageEnum) {
      Class<?> fieldType = field.getType();
      protobufValue = JReflectionUtils.runStaticMethod(fieldType, "forNumber",
          ((ProtocolMessageEnum) protobufValue).getNumber());
      argClazz = field.getType();
    } else {
      protobufValue.getClass();
    }
    JReflectionUtils.runSetter(pojo, setter, protobufValue, argClazz);
  }

  private static Object convertCollectionFromProtobufs(Field field,
      Collection<?> collectionOfProtobufs)
      throws JException, InstantiationException, IllegalAccessException {
    if (collectionOfProtobufs.isEmpty()) {
      return collectionOfProtobufs;
    }
    final ParameterizedType listType = (ParameterizedType) field.getGenericType();
    final Class<?> collectionClazzType = (Class<?>) listType.getActualTypeArguments()[0];
    final ProtobufEntity protoBufEntityAnno =
        ProtobufSerializerUtils.getProtobufEntity(collectionClazzType);
    final Object first = collectionOfProtobufs.toArray()[0];
    if (!(first instanceof Message) && protoBufEntityAnno == null) {
      return collectionOfProtobufs;
    }
    final Collection<Object> newCollectionOfValues = new ArrayList<>();
    for (Object protobufValue : collectionOfProtobufs) {
      if (!(protobufValue instanceof Message)) {
        throw new ProtobufException(
            "Collection contains an object of type " + protobufValue.getClass()
                + " which is not an instanceof GeneratedMessage, can not (de)serialize this");
      }
      newCollectionOfValues
          .add(serializeFromProtobufEntity((Message) protobufValue, collectionClazzType));
    }

    return newCollectionOfValues;
  }

}
