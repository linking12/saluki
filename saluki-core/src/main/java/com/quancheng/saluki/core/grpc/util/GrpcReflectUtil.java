/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author shimingliu 2016年12月14日 下午9:30:54
 * @version GrpcReflectUtil.java, v 0.0.1 2016年12月14日 下午9:30:54 shimingliu
 */
public final class GrpcReflectUtil {

    public static Class<?> getTypedReq(Method method) {
        Class<?>[] params = method.getParameterTypes();
        return params[0];
    }

    public static Class<?> getTypeRep(Method method) {
        return method.getReturnType();
    }

    public static Annotation findAnnotationFromClass(Class<?> target, Class<? extends Annotation> annotation) {
        for (Annotation targetAnnotation : target.getAnnotations()) {
            if (annotation.isAssignableFrom(targetAnnotation.annotationType())) {
                return targetAnnotation;
            } else {
                continue;
            }
        }
        return null;
    }

    public static Annotation findAnnotationFromMethod(Method method, Class<? extends Annotation> annotation) {
        for (Annotation targetAnnotation : method.getAnnotations()) {
            if (annotation.isAssignableFrom(targetAnnotation.annotationType())) {
                return targetAnnotation;
            } else {
                continue;
            }
        }
        return null;
    }

    public static Object classInstance(Class<?> clazz) {
        checkPackageAccess(clazz);
        try {
            Constructor<?> con = clazz.getDeclaredConstructor();
            con.setAccessible(true);
            Object obj = con.newInstance();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void checkPackageAccess(Class<?> clazz) {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            String cname = clazz.getName().replace('/', '.');
            if (cname.startsWith("[")) {
                int b = cname.lastIndexOf('[') + 2;
                if (b > 1 && b < cname.length()) {
                    cname = cname.substring(b);
                }
            }
            int i = cname.lastIndexOf('.');
            if (i != -1) {
                s.checkPackageAccess(cname.substring(0, i));
            }
        }
    }

    public static boolean isToStringMethod(Method method) {
        return (method != null && method.getName().equals("toString") && method.getParameterTypes().length == 0);
    }

    public static boolean isLegal(Method method) {
        return isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(method) || isObjectMethod(method);
    }

    public static boolean isEqualsMethod(Method method) {
        if (method == null || !method.getName().equals("equals")) {
            return false;
        }
        Class<?>[] paramTypes = method.getParameterTypes();
        return (paramTypes.length == 1 && paramTypes[0] == Object.class);
    }

    public static boolean isHashCodeMethod(Method method) {
        return (method != null && method.getName().equals("hashCode") && method.getParameterTypes().length == 0);
    }

    public static boolean isObjectMethod(Method method) {
        if (method == null) {
            return false;
        }
        try {
            Object.class.getDeclaredMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static List<Method> findAllPublicMethods(Class<?> clazz) {
        List<Method> methods = Lists.newLinkedList();
        for (Method method : clazz.getMethods()) {
            if (isLegal(method)) {
                continue;
            }
            methods.add(method);
        }
        return methods;
    }

    private GrpcReflectUtil(){
    }
}
