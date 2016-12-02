package com.quancheng.saluki.serializer.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JReflectionUtils {

    private JReflectionUtils(){
    }

    public static Object runGetter(Object object, Field field) throws IllegalAccessException, IllegalArgumentException,
                                                               InvocationTargetException {
        final Class<?> clazz = object.getClass();
        final String fieldName = field.getName();
        try {
            final Method method = clazz.getMethod(JStringUtils.GET + JStringUtils.upperCaseFirst(fieldName),
                                                  new Class<?>[] {});
            return method.invoke(object);
        } catch (Exception e) {
            // Swallow exception so that we loop through the rest.
        }
        for (Method method : clazz.getMethods()) {
            final String methodName = method.getName();
            if (((methodName.startsWith(JStringUtils.GET))
                 && (methodName.length() == (fieldName.length() + JStringUtils.GET.length())))
                || ((methodName.startsWith(JStringUtils.IS))
                    && (methodName.length() == (fieldName.length() + JStringUtils.IS.length())))) {
                if (methodName.toLowerCase().endsWith(fieldName.toLowerCase())) {
                    return method.invoke(object);
                }
            }
        }

        return null;
    }

    public static Object runSetter(Object object, String method, Object arg,
                                   Class<? extends Object> argClazz) throws JException {
        try {
            if (argClazz == null) {
                argClazz = arg.getClass();
            }
            final Method m = object.getClass().getMethod(method, argClazz);

            return m.invoke(object, arg);
        } catch (Exception e) {
            throw new JException(e);
        }
    }

    public static Object runMethod(Object object, String method, Object... args) throws JException {
        try {
            final Method m = object.getClass().getMethod(method);

            return m.invoke(object, args);
        } catch (Exception e) {
            throw new JException(e);
        }
    }

    /**
     * 执行静态方法
     */
    public static Object runStaticMethod(Class<?> clzz, String method, Object... args) throws JException {
        try {
            Class<?>[] parameterTypes = null;
            if (args.length != 0) {
                parameterTypes = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    parameterTypes[i] = args[i].getClass();
                }
            }
            Method m;
            if (parameterTypes == null) {
                m = clzz.getMethod(method);
            } else {
                m = clzz.getMethod(method, parameterTypes);
            }
            return m.invoke(null, args);
        } catch (Exception e) {
            throw new JException(e);
        }
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            fields.add(field);
        }

        if (clazz.getSuperclass() != null) {
            fields = getAllFields(fields, clazz.getSuperclass());
        }

        return fields;
    }

    public static List<Method> getAllMethods(List<Method> methods, Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            methods.add(method);
        }

        if (clazz.getSuperclass() != null) {
            methods = getAllMethods(methods, clazz.getSuperclass());
        }

        return methods;
    }

    public static Method getMethodByName(Class<?> clazz, String name) {
        final List<Method> methods = JReflectionUtils.getAllMethods(new ArrayList<Method>(), clazz);

        for (Method method : methods) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }
}
