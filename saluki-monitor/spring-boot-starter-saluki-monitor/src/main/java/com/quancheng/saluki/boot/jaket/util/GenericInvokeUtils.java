package com.quancheng.saluki.boot.jaket.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.boot.jaket.model.GenericInvokeMetadata;
import com.quancheng.saluki.boot.jaket.model.MetadataType;
import com.quancheng.saluki.boot.jaket.model.MethodDefinition;
import com.quancheng.saluki.boot.jaket.model.ServiceDefinition;
import com.quancheng.saluki.boot.jaket.model.TypeDefinition;

/**
 * @author bw on 11/25/15.
 */
public class GenericInvokeUtils {

    private static Logger  logger             = LoggerFactory.getLogger(GenericInvokeUtils.class);
    private static Pattern COLLECTION_PATTERN = Pattern.compile("^java\\.util\\..*(Set|List|Queue|Collection|Deque)(<.*>)*$");
    private static Pattern MAP_PATTERN        = Pattern.compile("^java\\.util\\..*Map.*(<.*>)*$");

    public static GenericInvokeMetadata getGenericInvokeMetadata(ServiceDefinition serviceDefinition,
                                                                 String methodSignature, MetadataType metadataType) {
        MethodDefinition methodDefinition = findMethodDefinition(serviceDefinition, methodSignature);
        if (methodDefinition == null) {
            return null;
        }

        GenericInvokeMetadata genericInvokeMetadata = new GenericInvokeMetadata();
        Object returnType = generateReturnType(serviceDefinition, methodDefinition, metadataType);
        genericInvokeMetadata.setReturnType(returnType);
        List<Object> parameterTypes = generateParameterTypes(serviceDefinition, methodDefinition, metadataType);
        genericInvokeMetadata.setParameterTypes(parameterTypes);
        genericInvokeMetadata.setSignature(methodSignature);
        return genericInvokeMetadata;
    }

    public static String[] findParameterTypes(String methodSignature, boolean erase) {
        String parameterStr = StringUtils.substringAfter(methodSignature, "~");
        String[] types = StringUtils.split(parameterStr, ';');
        if (erase) {
            for (int i = 0; i < types.length; i++) {
                types[i] = StringUtils.substringBefore(types[i], "<");
            }
        }
        return types;
    }

    public static String findMethodName(String methodSignature) {
        return StringUtils.substringBefore(methodSignature, "~");
    }

    static void generateComplexType(ServiceDefinition def, TypeDefinition td, Map<String, Object> map,
                                    MetadataType metadataType, Set<String> resolvedTypes) {
        Map<String, TypeDefinition> properties = td.getProperties();
        if (properties.isEmpty()) {
            logger.warn("unrecognized type: {}", td.getType());
            // FIXME: for unrecognized type, should we simply return without put its type in 'class' key?
        }

        String type = td.getType();
        map.put("class", type);
        if (resolvedTypes.contains(type)) {
            return;
        }
        resolvedTypes.add(type);
        properties.keySet().stream().forEach(key -> generateEnclosedType(def, key, properties.get(key), map,
                                                                         metadataType, resolvedTypes));
    }

    static Object generateType(ServiceDefinition def, String type, MetadataType metadataType,
                               Set<String> resolvedTypes) {
        TypeDefinition td = findTypeDefinition(def, type);
        return generateType(def, td, metadataType, resolvedTypes);
    }

    private static Object generateType(ServiceDefinition def, TypeDefinition td, MetadataType metadataType,
                                       Set<String> resolvedTypes) {
        String type = td.getType();
        return isPrimitiveType(td) ? generate(def, td,
                                              metadataType) : isArray(td) ? generateArrayType(def, type, metadataType,
                                                                                              resolvedTypes) : isCollection(td) ? generateCollectionType(def,
                                                                                                                                                         type, metadataType, resolvedTypes) : isMap(td) ? generateMapType(def,
                                                                                                                                                                                                                          td,
                                                                                                                                                                                                                          metadataType,
                                                                                                                                                                                                                          resolvedTypes) : isEnum(td) ? generate(def,
                                                                                                                                                                                                                                                                 td, metadataType) : isCircularReferenceType(td) ? generate(def,
                                                                                                                                                                                                                                                                                                                            td, metadataType) : generateComplexType(def,
                                                                                                                                                                                                                                                                                                                                                                    td,
                                                                                                                                                                                                                                                                                                                                                                    metadataType,
                                                                                                                                                                                                                                                                                                                                                                    resolvedTypes);
    }

    private static void generateEnclosedType(ServiceDefinition def, String key, TypeDefinition enclosedType,
                                             Map<String, Object> map, MetadataType metadataType,
                                             Set<String> resolvedTypes) {
        if (enclosedType.getProperties() == null || enclosedType.getProperties().isEmpty()) {
            map.put(key, generateType(def, enclosedType, metadataType, resolvedTypes));
        } else {
            Map<String, Object> enclosedMap = new HashMap<>();
            map.put(key, enclosedMap);
            generateComplexType(def, enclosedType, enclosedMap, metadataType, resolvedTypes);
        }
    }

    private static Object generate(ServiceDefinition def, TypeDefinition td, MetadataType metadataType) {
        if (metadataType == MetadataType.CLASS_TYPE) {
            return td.getType();
        } else if (metadataType == MetadataType.DEFAULT_VALUE) {
            if (isEnum(td)) {
                return generateEnumType(def, td);
            } else {
                return defaultValue(td.getType());
            }
        } else {
            logger.error("invalid metadata type: {}", metadataType);
            throw new IllegalArgumentException("invalid metadata type " + metadataType);
        }
    }

    private static Object generateComplexType(ServiceDefinition def, TypeDefinition td, MetadataType metadataType,
                                              Set<String> resolvedTypes) {
        Map<String, Object> map = new HashMap<>();
        if (resolvedTypes == Collections.EMPTY_SET) {
            resolvedTypes = new HashSet<>();
        }
        generateComplexType(def, td, map, metadataType, resolvedTypes);
        return map;
    }

    private static Object generateEnumType(ServiceDefinition def, TypeDefinition td) {
        return td.getEnums().get(0);
    }

    private static Object generateArrayType(ServiceDefinition def, String type, MetadataType metadataType,
                                            Set<String> resolvedTypes) {
        type = StringUtils.substringBeforeLast(type, "[]");
        return new Object[] { generateType(def, type, metadataType, resolvedTypes) };
    }

    private static Object generateCollectionType(ServiceDefinition def, String type, MetadataType metadataType,
                                                 Set<String> resolvedTypes) {
        type = StringUtils.substringAfter(type, "<");
        type = StringUtils.substringBefore(type, ">");
        if (StringUtils.isEmpty(type)) {
            type = "java.lang.Object";
        }
        return new Object[] { generateType(def, type, metadataType, resolvedTypes) };
    }

    // FIXME: cannot generate correct json string when the key is complex type
    private static Object generateMapType(ServiceDefinition def, TypeDefinition td, MetadataType metadataType,
                                          Set<String> resolvedTypes) {
        String keyType = StringUtils.substringAfter(td.getType(), "<");
        keyType = StringUtils.substringBefore(keyType, ",");
        keyType = StringUtils.strip(keyType);
        keyType = StringUtils.isNotEmpty(keyType) ? keyType : "java.lang.Object";
        Object key = generateType(def, keyType, metadataType, resolvedTypes);

        String valueType = StringUtils.substringAfter(td.getType(), ",");
        valueType = StringUtils.substringBefore(valueType, ">");
        valueType = StringUtils.strip(valueType);
        valueType = StringUtils.isNotEmpty(valueType) ? valueType : "java.lang.Object";
        Object value = generateType(def, valueType, metadataType, resolvedTypes);

        Map<Object, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private static Object defaultValue(String type) {
        switch (type) {
            case "short":
            case "java.lang.Short":
            case "int":
            case "java.lang.Integer":
            case "long":
            case "java.lang.Long":
                return 0;
            case "float":
            case "java.lang.Float":
            case "double":
            case "java.lang.Double":
                return 0.0;
            case "boolean":
            case "java.lang.Boolean":
                return true;
            case "java.lang.String":
                return "";
            case "java.lang.Object":
                return "{}";
            case "java.util.Date":
                return System.currentTimeMillis();
            default:
                return "{}";
        }
    }

    private static List<Object> generateParameterTypes(ServiceDefinition sd, MethodDefinition md,
                                                       MetadataType metadataType) {
        return Stream.of(md.getParameterTypes()).map(t -> generateType(sd, t, metadataType,
                                                                       Collections.emptySet())).collect(Collectors.toList());
    }

    private static Object generateReturnType(ServiceDefinition sd, MethodDefinition md, MetadataType metadataType) {
        return generateType(sd, md.getReturnType(), metadataType, Collections.emptySet());
    }

    private static TypeDefinition findTypeDefinition(ServiceDefinition def, String type) {
        return def.getTypes().stream().filter(t -> t.getType().equals(type)).findFirst().orElse(new TypeDefinition(type));
    }

    private static boolean isArray(TypeDefinition def) {
        return StringUtils.endsWith(def.getType(), "[]");
    }

    private static boolean isCircularReferenceType(TypeDefinition td) {
        return StringUtils.isNotEmpty(td.get$ref());
    }

    static boolean isCollection(TypeDefinition def) {
        Matcher matcher = COLLECTION_PATTERN.matcher(def.getType());
        return matcher.matches();
    }

    private static boolean isEnum(TypeDefinition td) {
        return td.getEnums() != null && !td.getEnums().isEmpty();
    }

    static boolean isMap(TypeDefinition def) {
        Matcher matcher = MAP_PATTERN.matcher(def.getType());
        return matcher.matches();
    }

    private static boolean isPrimitiveType(TypeDefinition def) {
        String type = def.getType();
        return type.equals("byte") || type.equals("java.lang.Byte") || type.equals("short")
               || type.equals("java.lang.Short") || type.equals("int") || type.equals("java.lang.Integer")
               || type.equals("long") || type.equals("java.lang.Long") || type.equals("float")
               || type.equals("java.lang.Float") || type.equals("double") || type.equals("java.lang.Double")
               || type.equals("boolean") || type.equals("java.lang.Boolean") || type.equals("void")
               || type.equals("java.lang.Void") || type.equals("java.lang.String") || type.equals("java.util.Date")
               || type.equals("java.lang.Object");
    }

    private static MethodDefinition findMethodDefinition(ServiceDefinition serviceDefinition, String methodSignature) {
        Optional<MethodDefinition> typeDefinition = serviceDefinition.getMethods().stream().filter(m -> m.getName().equals(findMethodName(methodSignature))
                                                                                                        && Arrays.equals(m.getParameterTypes(),
                                                                                                                         findParameterTypes(methodSignature,
                                                                                                                                            false))).findFirst();
        return typeDefinition.isPresent() ? typeDefinition.get() : null;
    }
}
