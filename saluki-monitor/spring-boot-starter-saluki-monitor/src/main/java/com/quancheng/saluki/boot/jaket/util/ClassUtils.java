package com.quancheng.saluki.boot.jaket.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangsheng.hs on 2015/1/27.
 */
public final class ClassUtils {

    /**
     * Get the code source file or class path of the Class passed in.
     *
     * @param clazz
     *            Class to find.
     * @return Jar file name or class path.
     */
    public static String getCodeSource(Class<?> clazz) {
        ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        if (protectionDomain == null || protectionDomain.getCodeSource() == null) {
            return null;
        }

        CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        URL location = codeSource.getLocation();
        if (location == null) {
            return null;
        }

        String path = codeSource.getLocation().toExternalForm();

        if (path.endsWith(".jar") && path.contains("/")) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return path;
    }

    /**
     * Get all non-static fields of the Class passed in or its super classes.
     *
     * @param clazz
     *            Class to parse.
     * @return field list
     */
    public static List<Field> getNonStaticFields(final Class<?> clazz) {
        List<Field> result = new ArrayList<Field>();
        Class<?> target = clazz;
        while (target != null) {
            if (JaketConfigurationUtils.isExcludedType(target)) {
                break;
            }

            Field[] fields = target.getDeclaredFields();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                    continue;
                }

                result.add(field);
            }
            target = target.getSuperclass();
        }

        return result;
    }

    /**
     * Get all public, non-static methods of the Class passed in.
     *
     * @param clazz
     *            Class to parse.
     * @return methods list
     */
    public static List<Method> getPublicMethods(final Class<?> clazz) {
        List<Method> result = new ArrayList<Method>();

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            int mod = method.getModifiers();
            if (Modifier.isPublic(mod)) {
                result.add(method);
            }
        }
        return result;
    }

    /**
     * Get all public, non-static methods of the Class passed in.
     *
     * @param clazz
     *            Class to parse.
     * @return methods list
     */
    public static List<Method> getPublicNonStaticMethods(final Class<?> clazz) {
        List<Method> result = new ArrayList<Method>();

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            int mod = method.getModifiers();
            if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
                result.add(method);
            }
        }
        return result;
    }

    private ClassUtils() {
    }
}
