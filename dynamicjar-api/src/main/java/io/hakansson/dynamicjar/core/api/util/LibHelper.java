package io.hakansson.dynamicjar.core.api.util;

import io.hakansson.dynamicjar.core.api.exception.DependencyResolutionException;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.logging.api.InternalLoggerBinder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by Erik Håkansson on 2016-05-09.
 * WirelessCar
 */
public class LibHelper {

    private static Logger logger = InternalLoggerBinder.getLogger(LibHelper.class);

    public static URL getOwnJar() {
        return LibHelper.class.getProtectionDomain().getCodeSource().getLocation();
    }

    public static URL[] getLibs(Class<?> caller, String path) throws DynamicJarException {
        try {
            if (!path.endsWith(".jar") && !path.endsWith(".ref") && !path.endsWith("/")) {
                path = path + "/";
            }
            List<URL> libs = new ArrayList<>();
            File ownFile = new File(caller.getProtectionDomain().getCodeSource().getLocation().toURI());
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
            Collections.sort(libs, (o1, o2) -> {
                if (o1.getPath().contains("nested-jar-classloader")) {
                    return -1;
                }
                return 0;
            });
            return libs.toArray(new URL[libs.size()]);
        } catch (IOException | URISyntaxException e) {
            throw new DependencyResolutionException(e);
        }
    }
}
