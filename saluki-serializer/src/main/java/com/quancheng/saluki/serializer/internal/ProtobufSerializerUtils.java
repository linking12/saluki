package com.quancheng.saluki.serializer.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.serializer.ProtobufAttribute;
import com.quancheng.saluki.serializer.ProtobufEntity;
import com.quancheng.saluki.serializer.utils.JReflectionUtils;
import com.quancheng.saluki.serializer.utils.JStringUtils;

public final class ProtobufSerializerUtils {


  private static final Map<String, Map<Field, ProtobufAttribute>> CLASS_TO_FIELD_MAP_CACHE =
      Collections.synchronizedMap(new WeakHashMap<String, Map<Field, ProtobufAttribute>>());

  // Internal cache to hold onto Class -> fieldName -> setter
  private static final Map<String, Map<String, String>> CLASS_TO_FIELD_SETTERS_MAP_CACHE =
      Collections.synchronizedMap(new WeakHashMap<String, Map<String, String>>());

  // Internal cache to hold onto Class -> fieldName -> getter
  private static final Map<String, Map<String, String>> CLASS_TO_FIELD_GETTERS_MAP_CACHE =
      Collections.synchronizedMap(new WeakHashMap<String, Map<String, String>>());


  public static final Class<? extends Object> getProtobufClass(Object value,
      Class<? extends Object> protobufClass) {
    if (value instanceof Integer) {
      return Integer.TYPE;
    }
    if (value instanceof Boolean) {
      return Boolean.TYPE;
    }
    if (value instanceof Double) {
      return Double.TYPE;
    }
    if (value instanceof Long || value instanceof Date) {
      return Long.TYPE;
    }
    if (value instanceof List) {
      return Iterable.class;
    }
    if (value instanceof Map) {
      return Map.class;
    }
    return protobufClass;
  }

  public static final ProtobufEntity getProtobufEntity(Class<?> clazz) {
    final ProtobufEntity protoBufEntity = clazz.getAnnotation(ProtobufEntity.class);
    if (protoBufEntity != null) {
      return protoBufEntity;
    }
    return null;
  }

  public static final boolean isProtbufEntity(Object object) {
    return isProtbufEntity(object.getClass());
  }

  public static final boolean isProtbufEntity(Class<?> clazz) {
    final ProtobufEntity protoBufEntity = getProtobufEntity(clazz);
    if (protoBufEntity != null) {
      return true;
    }
    return false;
  }

  public static final Class<? extends GeneratedMessageV3> getProtobufClassFromPojoAnno(
      Class<?> clazz) {
    final ProtobufEntity annotation = getProtobufEntity(clazz);
    final Class<? extends GeneratedMessageV3> gpbClazz =
        (Class<? extends GeneratedMessageV3>) annotation.value();
    if (gpbClazz == null) {
      return null;
    }
    return gpbClazz;
  }

  public static final Map<Field, ProtobufAttribute> getAllProtbufFields(
      Class<? extends Object> fromClazz) {
    Map<Field, ProtobufAttribute> protoBufFields =
        CLASS_TO_FIELD_MAP_CACHE.get(fromClazz.getCanonicalName());
    if (protoBufFields != null) {
      return protoBufFields;
    } else {
      protoBufFields = new HashMap<>();
    }
    final List<Field> fields = JReflectionUtils.getAllFields(new ArrayList<Field>(), fromClazz);
    for (Field field : fields) {
      final Annotation annotation = field.getAnnotation(ProtobufAttribute.class);
      if (annotation == null) {
        continue;
      }
      final ProtobufAttribute gpbAnnotation = (ProtobufAttribute) annotation;
      protoBufFields.put(field, gpbAnnotation);
    }
    CLASS_TO_FIELD_MAP_CACHE.put(fromClazz.getCanonicalName(), protoBufFields);
    return protoBufFields;
  }

  public static final String getProtobufSetter(ProtobufAttribute protobufAttribute, Field field,
      Object fieldValue) {
    final String fieldName = field.getName();
    final String upperClassName = field.getDeclaringClass().getCanonicalName();
    // Look at the cache first
    Map<String, String> map = CLASS_TO_FIELD_SETTERS_MAP_CACHE.get(upperClassName);
    if (map != null) {
      if (!map.isEmpty() && map.containsKey(fieldName)) {
        return map.get(fieldName);
      }
    } else {
      map = new ConcurrentHashMap<>();
    }
    String setter = "set" + JStringUtils.upperCaseFirst(fieldName);
    if (fieldValue instanceof Collection) {
      setter = "addAll" + JStringUtils.upperCaseFirst(fieldName);
    }
    if (fieldValue instanceof Map) {
      setter = "putAll" + JStringUtils.upperCaseFirst(fieldName);
    }
    final String configedSetter = protobufAttribute.protobufSetter();
    if (!configedSetter.equals(JStringUtils.EMPTY)) {
      setter = configedSetter;
    }
    CLASS_TO_FIELD_SETTERS_MAP_CACHE.put(upperClassName, map);
    return setter;
  }

  public static final String getProtobufGetter(ProtobufAttribute protobufAttribute, Field field) {
    final String fieldName = field.getName();
    final String upperClassName = field.getDeclaringClass().getCanonicalName();
    // Look at the cache first
    Map<String, String> map = CLASS_TO_FIELD_GETTERS_MAP_CACHE.get(upperClassName);
    if (map != null) {
      if (!map.isEmpty() && map.containsKey(fieldName)) {
        return map.get(fieldName);
      }
    } else {
      map = new ConcurrentHashMap<>();
    }
    final String upperCaseFirstFieldName = JStringUtils.upperCaseFirst(field.getName());
    String getter = "get" + upperCaseFirstFieldName;
    if (Collection.class.isAssignableFrom(field.getType())) {
      getter += "List";
    }
    if (!protobufAttribute.protobufGetter().isEmpty()) {
      return protobufAttribute.protobufGetter();
    }
    CLASS_TO_FIELD_GETTERS_MAP_CACHE.put(upperClassName, map);
    return getter;
  }

  public static final String getPojoSetter(ProtobufAttribute protobufAttribute, Field field) {
    final String fieldName = field.getName();
    final String upperClassName = field.getDeclaringClass().getCanonicalName();
    // Look at the cache first
    Map<String, String> map = CLASS_TO_FIELD_SETTERS_MAP_CACHE.get(upperClassName);
    if (map != null) {
      if (!map.isEmpty() && map.containsKey(fieldName)) {
        return map.get(fieldName);
      }
    } else {
      map = new ConcurrentHashMap<>();
    }
    final String upperCaseFirstFieldName = JStringUtils.upperCaseFirst(field.getName());
    String setter = "set" + upperCaseFirstFieldName;
    if (!protobufAttribute.pojoSetter().isEmpty()) {
      return protobufAttribute.pojoSetter();
    }
    CLASS_TO_FIELD_SETTERS_MAP_CACHE.put(upperClassName, map);
    return setter;
  }
}
