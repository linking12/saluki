package com.quancheng.saluki.serializer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.ProtocolMessageEnum;
import com.quancheng.saluki.serializer.converter.NullConverter;
import com.quancheng.saluki.serializer.exception.ProtobufAnnotationException;
import com.quancheng.saluki.serializer.exception.ProtobufException;
import com.quancheng.saluki.serializer.internal.ProtobufSerializerUtils;
import com.quancheng.saluki.serializer.utils.JException;
import com.quancheng.saluki.serializer.utils.JReflectionUtils;

public class ProtobufSerializer implements IProtobufSerializer {

    @Override
    public Message toProtobuf(Object pojo) throws ProtobufException {
        try {
            final Class<?> fromClazz = (Class<?>) pojo.getClass();
            final Class<? extends Message> protoClazz = ProtobufSerializerUtils.getProtobufClassFromPojoAnno(fromClazz);
            if (protoClazz == null) {
                throw new ProtobufAnnotationException("Doesn't seem like " + fromClazz + " is ProtobufEntity");
            }

            final Map<Field, ProtobufAttribute> protoBufFields = ProtobufSerializerUtils.getAllProtbufFields(fromClazz);
            final Method newBuilderMethod = protoClazz.getMethod("newBuilder");
            final Builder protoObjBuilder = (Builder) newBuilderMethod.invoke(null);
            if (protoBufFields.isEmpty()) {
                return protoObjBuilder.build();
            } else {
                for (Entry<Field, ProtobufAttribute> entry : protoBufFields.entrySet()) {
                    final Field field = entry.getKey();
                    final ProtobufAttribute gpbAnnotation = entry.getValue();
                    // 1. Determine validity of value
                    Object value = getPojoFieldValue(pojo, gpbAnnotation, field);
                    // If value is null and it is not required, skip, as the default for Protobuf values is null
                    if (value == null) {
                        continue;
                    }
                    // 2. Call recursively if this is a ProtobufEntity
                    value = serializeToProtobufEntity(value);
                    // 3. Handle POJO Collections/Lists
                    if (value instanceof Collection) {
                        value = convertCollectionToProtobufs((Collection<Object>) value);
                        if (((Collection) value).isEmpty()) {
                            continue;
                        }
                    }
                    // 4. Determine the setter name
                    final String setter = ProtobufSerializerUtils.getProtobufSetter(gpbAnnotation, field, value);
                    if (value instanceof Enum) {
                        value = JReflectionUtils.runMethod(value, "getNumber");
                    }
                    // 5. Finally, set the value on the Builder
                    setProtobufFieldValue(gpbAnnotation, protoObjBuilder, setter, value);
                }
                return protoObjBuilder.build();
            }

        } catch (Exception e) {
            throw new ProtobufException("Could not generate Protobuf object for " + pojo.getClass() + ": " + e, e);
        }
    }

    @Override
    public Object fromProtobuf(Message protobuf, Class<?> pojoClazz) throws ProtobufException {
        try {
            final Class<? extends Message> protoClazz = ProtobufSerializerUtils.getProtobufClassFromPojoAnno(pojoClazz);
            if (protoClazz == null) {
                throw new ProtobufAnnotationException("Doesn't seem like " + pojoClazz + " is ProtobufEntity");
            }

            final Map<Field, ProtobufAttribute> protobufFields = ProtobufSerializerUtils.getAllProtbufFields(pojoClazz);
            Object pojo = pojoClazz.newInstance();
            // 如果是空对象，直接构造一个空对象返回出去
            if (protobufFields.isEmpty()) {
                return pojo;
            } else {
                for (Entry<Field, ProtobufAttribute> entry : protobufFields.entrySet()) {
                    final Field field = entry.getKey();
                    final ProtobufAttribute protobufAttribute = entry.getValue();
                    final String setter = ProtobufSerializerUtils.getPojoSetter(protobufAttribute, field);
                    Object protobufValue = getProtobufFieldValue(protobuf, protobufAttribute, field);
                    if (protobufValue == null) {
                        continue;
                    }
                    if (protobufValue instanceof Message) {
                        Class<?> pojoClzz = field.getType();
                        Object potoValue = fromProtobuf((Message) protobufValue, pojoClzz);
                        setPojoFieldValue(pojo, setter, potoValue, protobufAttribute);
                        // 对于对象自己嵌自己的，只转换一次，防止重复递归
                        if (pojoClazz.equals(pojoClzz)) {
                            continue;
                        }
                    } else if (protobufValue instanceof ProtocolMessageEnum) {
                        Class<?> enumClzz = field.getType();
                        ProtocolMessageEnum protocolEnum = (ProtocolMessageEnum) protobufValue;
                        Object enumValue = JReflectionUtils.runStaticMethod(enumClzz, "forNumber",
                                                                            protocolEnum.getNumber());
                        setPojoFieldValue(pojo, setter, enumValue, protobufAttribute);
                    } else {
                        setPojoFieldValue(pojo, setter, protobufValue, protobufAttribute);
                    }
                }
                return pojo;
            }
        } catch (Exception e) {
            throw new ProtobufException("Could not generate POJO of type " + pojoClazz + " from Protobuf object "
                                        + protobuf.getClass() + ": " + e, e);
        }
    }

    private static final Object getPojoFieldValue(Object pojo, ProtobufAttribute protobufAttribute,
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

    @SuppressWarnings("rawtypes")
    private static final Object getProtobufFieldValue(Message protoBuf, ProtobufAttribute protobufAttribute,
                                                      Field field) throws JException, InstantiationException,
                                                                   IllegalAccessException {
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
        if (protobufValue instanceof Message && ProtobufSerializerUtils.isProtbufEntity(field.getType())) {
            protobufValue = serializeFromProtobufEntity((Message) protobufValue, field.getType());
        }

        if (protobufValue instanceof Collection) {
            protobufValue = convertCollectionFromProtobufs(field, (Collection<?>) protobufValue);
            if (((Collection) protobufValue).isEmpty()) {
                return null;
            }
        }

        return protobufValue;
    }

    private static final Object serializeToProtobufEntity(Object pojo) throws JException {
        final ProtobufEntity protoBufEntity = ProtobufSerializerUtils.getProtobufEntity(pojo.getClass());

        if (protoBufEntity == null) {
            return pojo;
        }

        return new ProtobufSerializer().toProtobuf(pojo);
    }

    private static final Object serializeFromProtobufEntity(Message protoBuf, Class<?> pojoClazz) throws JException {
        final ProtobufEntity protoBufEntity = ProtobufSerializerUtils.getProtobufEntity(pojoClazz);

        if (protoBufEntity == null) {
            return protoBuf;
        }

        return new ProtobufSerializer().fromProtobuf(protoBuf, pojoClazz);
    }

    private static final Object convertCollectionToProtobufs(Collection<Object> collectionOfNonProtobufs) throws JException {
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
            newCollectionValues.add(serializeToProtobufEntity(iProtobufGenObj));
        }
        return newCollectionValues;
    }

    private static Object convertCollectionFromProtobufs(Field field,
                                                         Collection<?> collectionOfProtobufs) throws JException,
                                                                                              InstantiationException,
                                                                                              IllegalAccessException {
        if (collectionOfProtobufs.isEmpty()) {
            return collectionOfProtobufs;
        }
        final ParameterizedType listType = (ParameterizedType) field.getGenericType();
        final Class<?> collectionClazzType = (Class<?>) listType.getActualTypeArguments()[0];
        final ProtobufEntity protoBufEntityAnno = ProtobufSerializerUtils.getProtobufEntity(collectionClazzType);
        final Object first = collectionOfProtobufs.toArray()[0];
        if (!(first instanceof Message) && protoBufEntityAnno == null) {
            return collectionOfProtobufs;
        }
        final Collection<Object> newCollectionOfValues = new ArrayList<>();
        for (Object protobufValue : collectionOfProtobufs) {
            if (!(protobufValue instanceof Message)) {
                throw new ProtobufException("Collection contains an object of type " + protobufValue.getClass()
                                            + " which is not an instanceof GeneratedMessage, can not (de)serialize this");
            }
            newCollectionOfValues.add(serializeFromProtobufEntity((Message) protobufValue, collectionClazzType));
        }

        return newCollectionOfValues;
    }

    private static final void setProtobufFieldValue(ProtobufAttribute protobufAttribute, Builder protoObjBuilder,
                                                    String setter,
                                                    Object fieldValue) throws NoSuchMethodException, SecurityException,
                                                                       ProtobufAnnotationException,
                                                                       InstantiationException, IllegalAccessException,
                                                                       IllegalArgumentException,
                                                                       InvocationTargetException {
        Class<? extends Object> fieldValueClass = fieldValue.getClass();
        Class<? extends Object> gpbClass = fieldValueClass;

        final Class<? extends IProtobufConverter> converterClazz = protobufAttribute.converter();
        if (converterClazz != NullConverter.class) {
            final IProtobufConverter protoBufConverter = (IProtobufConverter) converterClazz.newInstance();
            fieldValue = protoBufConverter.convertToProtobuf(fieldValue);
            gpbClass = fieldValue.getClass();
            fieldValueClass = gpbClass;
        }

        // Need to convert the argument class from non-primitives to primitives, as Protobuf uses these.
        gpbClass = ProtobufSerializerUtils.getProtobufClass(fieldValue, gpbClass);

        final Method gpbMethod = protoObjBuilder.getClass().getDeclaredMethod(setter, gpbClass);
        gpbMethod.invoke(protoObjBuilder, fieldValue);
    }

    private static final void setPojoFieldValue(Object pojo, String setter, Object protobufValue,
                                                ProtobufAttribute protobufAttribute) throws InstantiationException,
                                                                                     IllegalAccessException,
                                                                                     JException {
        final Class<? extends IProtobufConverter> fromProtoBufConverter = protobufAttribute.converter();
        if (fromProtoBufConverter != NullConverter.class) {
            final IProtobufConverter converter = fromProtoBufConverter.newInstance();
            protobufValue = converter.convertFromProtobuf(protobufValue);
        }
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
        } else {
            protobufValue.getClass();
        }
        JReflectionUtils.runSetter(pojo, setter, protobufValue, argClazz);
    }
}
