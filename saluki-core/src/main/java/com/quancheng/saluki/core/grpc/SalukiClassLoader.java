package com.quancheng.saluki.core.grpc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

public class SalukiClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }
    private final Set<URL>    addedURL    = Sets.newConcurrentHashSet();
    private final ClassLoader _parent;
    private final Set<String> _extensions = new HashSet<String>();

    public SalukiClassLoader(){
        super(new URL[] {}, Thread.currentThread().getContextClassLoader());
        _parent = getParent();
        _extensions.add(".jar");
        _extensions.add(".zip");
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    public Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
            try {
                c = _parent.loadClass(name);
                if (resolve) resolveClass(c);
            } catch (ClassNotFoundException e) {
                throw e;
            }
            return findClass(name);
        }
    }

    private boolean isFileSupported(String file) {
        int dot = file.lastIndexOf('.');
        return dot != -1 && _extensions.contains(file.substring(dot));
    }

    public void addClassPath() throws IOException {
        String jarDirectoryPath = System.getProperty("user.home") + File.pathSeparator + "saluki";
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
