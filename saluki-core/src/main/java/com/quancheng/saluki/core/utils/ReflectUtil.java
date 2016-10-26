package com.quancheng.saluki.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Lists;

public final class ReflectUtil {

    private ReflectUtil(){
    }

    public static Object classInstance(Class cls) {
        checkPackageAccess(cls);
        try {
            Constructor con = cls.getDeclaredConstructor();
            con.setAccessible(true);
            Object obj = con.newInstance();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class<?> getTypedReq(Method method) {
        Class[] params = method.getParameterTypes();
        return params[0];
    }

    public static Class<?> getTypeRep(Method method) {
        return method.getReturnType();
    }

    public static Object newMethodReq(Method method) {
        Class[] params = method.getParameterTypes();
        List<Object> objs = Lists.newArrayList();
        for (Class cls : params) {
            checkPackageAccess(cls);
            try {
                Constructor con = cls.getDeclaredConstructor();
                con.setAccessible(true);
                Object obj = con.newInstance();
                objs.add(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return objs.get(0);
    }

    public static Object newMethodRep(Method method) {
        Class cls = method.getReturnType();
        checkPackageAccess(cls);
        Object obj = null;
        try {
            Constructor con = cls.getDeclaredConstructor();
            con.setAccessible(true);
            obj = con.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
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

    public static List<Method> findAllPublicMethods(Class<?> clazz) {
        List<Method> methods = Lists.newLinkedList();
        for (Method method : clazz.getMethods()) {
            if (neglectMethod(method)) {
                continue;
            }
            methods.add(method);
        }
        return methods;
    }

    public static boolean neglectMethod(Method method) {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        return neglectMethod(methodName, parameterTypes);
    }

    public static boolean neglectMethod(String methodName, Class<?>[] parameterTypes) {
        boolean isToString = "toString".equals(methodName) && parameterTypes.length == 0;
        boolean isHashCode = "hashCode".equals(methodName) && parameterTypes.length == 0;
        boolean isEquals = "equals".equals(methodName) && parameterTypes.length == 1;
        boolean isnotify = "notify".equals(methodName) && parameterTypes.length == 0;
        boolean isnotifyAll = "notifyAll".equals(methodName) && parameterTypes.length == 0;
        boolean isgetClass = "getClass".equals(methodName) && parameterTypes.length == 0;
        boolean iswait = "wait".equals(methodName)
                         && (parameterTypes.length == 0 || parameterTypes.length == 1 || parameterTypes.length == 2);
        if (isToString || isHashCode || isEquals || isnotify || isnotifyAll || isgetClass || iswait) {
            return true;
        } else {
            return false;
        }
    }

    private static void checkPackageAccess(Class clazz) {
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

    /**
     * void(V).
     */
    public static final char                             JVM_VOID         = 'V';

    /**
     * boolean(Z).
     */
    public static final char                             JVM_BOOLEAN      = 'Z';

    /**
     * byte(B).
     */
    public static final char                             JVM_BYTE         = 'B';

    /**
     * char(C).
     */
    public static final char                             JVM_CHAR         = 'C';

    /**
     * double(D).
     */
    public static final char                             JVM_DOUBLE       = 'D';

    /**
     * float(F).
     */
    public static final char                             JVM_FLOAT        = 'F';

    /**
     * int(I).
     */
    public static final char                             JVM_INT          = 'I';

    /**
     * long(J).
     */
    public static final char                             JVM_LONG         = 'J';

    /**
     * short(S).
     */
    public static final char                             JVM_SHORT        = 'S';

    private static final ConcurrentMap<String, Class<?>> NAME_CLASS_CACHE = new ConcurrentHashMap<String, Class<?>>();

    public static Class<?> forName(String name) {
        try {
            return name2class(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Not found class " + name + ", cause: " + e.getMessage(), e);
        }
    }

    /**
     * name to class. "boolean" => boolean.class "java.util.Map[][]" => java.util.Map[][].class
     * 
     * @param name name.
     * @return Class instance.
     */
    public static Class<?> name2class(String name) throws ClassNotFoundException {
        return name2class(ClassHelper.getClassLoader(), name);
    }

    /**
     * name to class. "boolean" => boolean.class "java.util.Map[][]" => java.util.Map[][].class
     * 
     * @param cl ClassLoader instance.
     * @param name name.
     * @return Class instance.
     */
    private static Class<?> name2class(ClassLoader cl, String name) throws ClassNotFoundException {
        int c = 0, index = name.indexOf('[');
        if (index > 0) {
            c = (name.length() - index) / 2;
            name = name.substring(0, index);
        }
        if (c > 0) {
            StringBuilder sb = new StringBuilder();
            while (c-- > 0)
                sb.append("[");

            if ("void".equals(name)) sb.append(JVM_VOID);
            else if ("boolean".equals(name)) sb.append(JVM_BOOLEAN);
            else if ("byte".equals(name)) sb.append(JVM_BYTE);
            else if ("char".equals(name)) sb.append(JVM_CHAR);
            else if ("double".equals(name)) sb.append(JVM_DOUBLE);
            else if ("float".equals(name)) sb.append(JVM_FLOAT);
            else if ("int".equals(name)) sb.append(JVM_INT);
            else if ("long".equals(name)) sb.append(JVM_LONG);
            else if ("short".equals(name)) sb.append(JVM_SHORT);
            else sb.append('L').append(name).append(';'); // "java.lang.Object" ==> "Ljava.lang.Object;"
            name = sb.toString();
        } else {
            if ("void".equals(name)) return void.class;
            else if ("boolean".equals(name)) return boolean.class;
            else if ("byte".equals(name)) return byte.class;
            else if ("char".equals(name)) return char.class;
            else if ("double".equals(name)) return double.class;
            else if ("float".equals(name)) return float.class;
            else if ("int".equals(name)) return int.class;
            else if ("long".equals(name)) return long.class;
            else if ("short".equals(name)) return short.class;
        }

        if (cl == null) cl = ClassHelper.getClassLoader();
        Class<?> clazz = NAME_CLASS_CACHE.get(name);
        if (clazz == null) {
            clazz = Class.forName(name, true, cl);
            NAME_CLASS_CACHE.put(name, clazz);
        }
        return clazz;
    }

}
