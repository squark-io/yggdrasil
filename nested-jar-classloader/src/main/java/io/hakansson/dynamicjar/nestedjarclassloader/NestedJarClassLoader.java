package io.hakansson.dynamicjar.nestedjarclassloader;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-04-11.
 * Copyright 2016
 */

import io.hakansson.dynamicjar.logging.api.InternalLogger;
import io.hakansson.dynamicjar.logging.api.LogLevel;
import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-04-11.
 * Copyright 2016
 */
public class NestedJarClassLoader extends ClassLoader {

    private final Map<String, URL> jarResources = new HashMap<>();
    private final MultiValuedMap<String, URL> jarContents = MultiMapUtils.newListValuedHashMap();
    private InternalLogger logger = InternalLogger.getLogger(NestedJarClassLoader.class);

    public NestedJarClassLoader(URL[] urls, ClassLoader parent) {
        super(parent);
        addURLs(urls);
    }

    public void addURLs(URL[] urls) {
        for (URL url : urls) {
            addURL(url);
        }
    }

    public synchronized void addURL(URL url) {
        synchronized (jarResources) {
            if (!jarResources.containsKey(url.getPath())) {
                jarResources.put(url.getPath(), url);
                if (url.getPath().endsWith(".jar")) {
                    try {
                        addJar(url);
                    } catch (IOException e) {
                        logger.log(LogLevel.ERROR, e);
                        throw new RuntimeException(e);
                    }
                } else {
                    addResource(url);
                }
            } else {
                logger.log(LogLevel.WARN, "Already added " + url.getFile());
            }
        }
    }

    private synchronized void addResource(URL url) {
        synchronized (jarContents) {
            logger.log(LogLevel.TRACE, "Adding url " + url.getPath());
            String contentName;
            if (url.getProtocol().equals("jar")) {
                int li = url.getPath().lastIndexOf("!/");
                contentName = url.getPath().substring(li + 2);
            } else {
                contentName = url.getPath();
            }
            if (jarContents.containsKey(contentName)) {
                logger.log(LogLevel.TRACE, "Already have resource " + contentName +
                                           ". If different versions, unexpected behaviour might " +
                                           "occur. Available in " + jarContents.get(contentName));
            }
            jarContents.put(contentName, url);
        }
    }

    private synchronized void addJar(URL url) throws IOException {
        synchronized (jarContents) {
            logger.log(LogLevel.TRACE, "Adding url " + url.getPath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(url.openStream());
            JarInputStream jarInputStream = new JarInputStream(bufferedInputStream);
            JarEntry jarEntry;

            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                if (jarEntry.isDirectory()) {
                    continue;
                }
                if (jarContents.containsKey(jarEntry.getName())) {
                    logger.log(LogLevel.TRACE, "Already have resource " + jarEntry.getName() +
                                               ". If different versions, unexpected behaviour " +
                                               "might occur. Available in " +
                                               jarContents.get(jarEntry.getName()));
                }

                byte[] b = new byte[2048];
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                int len = 0;
                while ((len = jarInputStream.read(b)) > 0) {
                    out.write(b, 0, len);
                }
                String spec;
                if (url.getProtocol().equals("jar")) {
                    spec = url.getPath();
                } else {
                    spec = url.getProtocol() + ":" + url.getPath();
                }
                URL contentUrl = new URL(null, "jar:" + spec + "!/" + jarEntry.getName(),
                    new NestedJarURLStreamHandler());
                jarContents.put(jarEntry.getName(), contentUrl);
                if (jarEntry.getName().endsWith(".jar")) {
                    addJar(contentUrl);
                }
                out.close();
            }
            jarInputStream.close();
            bufferedInputStream.close();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        URL resourceURL = getResource(name.replace(".", "/") + ".class");
        if (resourceURL != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int len;
            byte[] b = new byte[2048];
            try {
                InputStream inputStream = resourceURL.openStream();
                while ((len = inputStream.read(b)) > 0) {
                    byteArrayOutputStream.write(b, 0, len);
                }
                byteArrayOutputStream.close();
                byte[] classBytes = byteArrayOutputStream.toByteArray();
                definePackageForClass(name);
                return defineClass(name, classBytes, 0, classBytes.length,
                    this.getClass().getProtectionDomain());
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    public URL findResource(String name) {
        try {
            synchronized (jarContents) {
                Enumeration<URL> urls = findResources(name);
                if (urls.hasMoreElements()) {
                    return urls.nextElement();
                }
            }
        } catch (IOException e) {
            // Do nothing
        }
        return null;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        synchronized (jarContents) {
            if (jarContents.containsKey(name)) {
                return Collections.enumeration(jarContents.get(name));
            }
            return Collections.emptyEnumeration();
        }
    }

    private void definePackageForClass(String className) {
        int i = className.lastIndexOf('.');
        if (i != -1) {
            String pkgname = className.substring(0, i);
            //Check if already defined:
            Package pkg = getPackage(pkgname);
            if (pkg == null) {
                definePackage(pkgname, null, null, null, null, null, null, null);
            }
        }
    }

}
