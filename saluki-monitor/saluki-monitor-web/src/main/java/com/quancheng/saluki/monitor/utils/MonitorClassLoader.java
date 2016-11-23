package com.quancheng.saluki.monitor.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

public class MonitorClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    private final Map<String, Class<?>> cachedClasses = new ConcurrentReferenceHashMap<String, Class<?>>();

    private final Set<URL>              addedURL      = Sets.newConcurrentHashSet();

    private final Set<String>           _extensions   = new HashSet<String>();

    public MonitorClassLoader(){
        super(new URL[] {}, Thread.currentThread().getContextClassLoader());
        _extensions.add(".jar");
        _extensions.add(".zip");
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (cachedClasses.get(name) != null) {
            return cachedClasses.get(name);
        } else {
            Class<?> clazz = loadClass(name, false);
            cachedClasses.put(name, clazz);
            return clazz;
        }
    }

    private boolean isFileSupported(String file) {
        int dot = file.lastIndexOf('.');
        return dot != -1 && _extensions.contains(file.substring(dot));
    }

    public void addClassPath() throws IOException {
        String jarDirectoryPath = System.getProperty("user.home") + "/saluki";
        File jarDirectory = new File(jarDirectoryPath);
        if (jarDirectory.exists() && jarDirectory.isDirectory()) {
            File[] jars = jarDirectory.listFiles();
            for (File jar : jars) {
                if (isFileSupported(jar.getName())) {
                    URL url = jar.toURI().toURL();
                    if (addedURL.contains(url)) {
                        continue;
                    } else {
                        addedURL.add(url);
                        addURL(url);
                    }
                }
            }
        }
    }

}
