package io.hakansson.dynamicjar.core.main.util;

import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.exception.NestedJarClassloaderException;
import io.hakansson.dynamicjar.core.main.DynamicJar;
import io.hakansson.dynamicjar.logging.api.InternalLogger;
import io.hakansson.dynamicjar.logging.api.LogLevel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by Erik Håkansson on 2016-05-09.
 * WirelessCar
 */
public class LibHelper {

    private static InternalLogger logger = InternalLogger.getLogger(LibHelper.class);

    public static URL[] getLibs(String path) throws DynamicJarException {
        try {
            if (!path.endsWith("/")) {
                path = path + "/";
            }
            Set<URL> libs = new HashSet<>();
            File ownFile = new File(DynamicJar.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            JarFile ownJar = new JarFile(ownFile);

            Enumeration<JarEntry> entries = ownJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith(path)) {
                    continue;
                }
                if (entry.getName().endsWith(".jar")) {
                    logger.log(LogLevel.DEBUG, "Found lib " + entry.getName());
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
}
