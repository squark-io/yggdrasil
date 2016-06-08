package io.hakansson.dynamicjar.core.api.util;

import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.exception.NestedJarClassloaderException;
import io.hakansson.dynamicjar.logging.api.InternalLoggerBinder;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassLoader;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by Erik Håkansson on 2016-05-09.
 * WirelessCar
 */
public class LibHelper {

    private static Logger logger = InternalLoggerBinder.getLogger(LibHelper.class);

    public static URL[] getLibs(String path) throws DynamicJarException {
        try {
            if (!path.endsWith("/")) {
                path = path + "/";
            }
            Set<URL> libs = new HashSet<>();
            File ownFile = new File(LibHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            JarFile ownJar = new JarFile(ownFile);

            Enumeration<JarEntry> entries = ownJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith(path)) {
                    continue;
                }
                if (entry.getName().endsWith(".jar")) {
                    logger.debug("Found lib " + entry.getName());
                    URL url = new URL("jar", "", ownFile.toURI().toString() + "!/" + entry.getName());
                    libs.add(url);
                } else if (entry.getName().endsWith(".ref")) {
                    InputStream inputStream = ownJar.getInputStream(entry);
                    Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                    if (scanner.hasNext()) {
                        String target = scanner.next();
                        URL url = new URL("jar", "", ownFile.toURI().toString() + "!/" + target);
                        libs.add(url);
                    }
                }
            }
            return libs.toArray(new URL[libs.size()]);
        } catch (IOException | URISyntaxException e) {
            throw new NestedJarClassloaderException(e);
        }
    }

    public static void copyResourcesIntoClassLoader(NestedJarClassLoader coreClassLoader, String path,
                                                    List<String> blacklist) throws NestedJarClassloaderException
    {
        try {

            if (!path.endsWith("/")) {
                path = path + "/";
            }
            File ownFile = new File(LibHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            JarFile ownJar = new JarFile(ownFile);

            Enumeration<JarEntry> entries = ownJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith(path) || entry.getName().endsWith("/")) {
                    continue;
                }
                boolean skip = false;
                for (String illegal : blacklist) {
                    if (entry.getName().startsWith(illegal)) {
                        skip = true;
                        continue;
                    }
                }
                if (skip) continue;
                URL url = new URL("jar", "", ownFile.toURI().toString() + "!/" + entry.getName());
                coreClassLoader.addURL(url);
            }
        } catch (IOException | URISyntaxException e) {
            throw new NestedJarClassloaderException(e);
        }
    }
}
