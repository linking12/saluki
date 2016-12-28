package com.quancheng.saluki.boot.jaket.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by huangsheng.hs on 2015/1/27.
 */
public class JaketConfigurationUtils {

    private static final String CONFIGURATION_FILE = "jaket.properties";

    private static String[] includedInterfacePackages;
    private static String[] includedTypePackages;
    private static String[] closedTypes;

    static {
        Properties props = new Properties();
        InputStream inStream = JaketConfigurationUtils.class.getClassLoader().getResourceAsStream(CONFIGURATION_FILE);
        try {
            props.load(inStream);
            String value = (String) props.get("included_interface_packages");
            if (value != null && !value.isEmpty()) {
                includedInterfacePackages = value.split(",");
            }

            value = props.getProperty("included_type_packages");
            if (value != null && !value.isEmpty()) {
                includedTypePackages = value.split(",");
            }

            value = props.getProperty("closed_types");
            if (value != null && !value.isEmpty()) {
                closedTypes = value.split(",");
            }

        } catch (Throwable e) {
            // Ignore it.
        }
    }

    public static boolean isExcludedInterface(Class<?> clazz) {
        if (includedInterfacePackages == null || includedInterfacePackages.length == 0) {
            return false;
        }

        for (String packagePrefix : includedInterfacePackages) {
            if (clazz.getCanonicalName().startsWith(packagePrefix)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isExcludedType(Class<?> clazz) {
        if (includedTypePackages == null || includedTypePackages.length == 0) {
            return false;
        }

        for (String packagePrefix : includedTypePackages) {
            if (clazz.getCanonicalName().startsWith(packagePrefix)) {
                return false;
            }
        }

        return true;
    }

    public static boolean needAnalyzing(Class<?> clazz) {
        String canonicalName = clazz.getCanonicalName();

        if (closedTypes != null && closedTypes.length > 0) {
            for (String type : closedTypes) {
                if (canonicalName.startsWith(type)) {
                    return false;
                }
            }
        }

        return !isExcludedType(clazz);
    }

}
