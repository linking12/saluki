package com.quancheng.saluki.core.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class ReflectUtil {

    private ReflectUtil(){
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

    public static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (result == null) {
                    result = new LinkedList<Method>();
                }
                result.add(ifcMethod);
            }
        }
        return result;
    }

    private static void checkPackageAccess(Class clazz) {
        checkPackageAccess(clazz.getName());
    }

    private static void checkPackageAccess(String name) {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            String cname = name.replace('/', '.');
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

}
