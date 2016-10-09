/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.squark.yggdrasil.core.api.util;

import io.squark.yggdrasil.core.api.YggdrasilContext;
import io.squark.yggdrasil.core.api.exception.DependencyResolutionException;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

    public static URL[] getLibs(Class<?> caller, String path) throws YggdrasilException {
        try {
            if (!path.endsWith(".jar") && !path.endsWith(".ref") && !path.endsWith("/")) {
                path = path + "/";
            }
            List<URL> libs;
            File ownFile;
            if (YggdrasilContext.getOverriddenLibraryPath() != null) {
                ownFile = new File(YggdrasilContext.getOverriddenLibraryPath().toURI());
            } else {
                ownFile = new File(caller.getProtectionDomain().getCodeSource().getLocation().toURI());
            }
            if (ownFile.isDirectory()) {
                libs = scan(new File(ownFile, path));
            } else {
                libs = new ArrayList<>();
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

    private static List<URL> scan(File ownFile) throws MalformedURLException, FileNotFoundException {
        List<URL> libs = new ArrayList<>();
        if (!ownFile.exists()) {
            return libs;
        }
        if (ownFile.isDirectory()) {
            libs.addAll(scanDir(ownFile));
            return libs;
        }
        libs.add(ownFile.toURI().toURL());
        return libs;
    }

    private static List<URL> scanDir(File ownFile) throws MalformedURLException, FileNotFoundException {
        List<URL> libs = new ArrayList<>();
        if (!ownFile.exists()) {
            return libs;
        }
        File[] files = ownFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    libs.addAll(scanDir(file));
                } else if (file.getName().endsWith(".jar")) {
                    logger.debug("Found lib " + file.getName());
                    URL url = file.toURI().toURL();
                    libs.add(url);
                } else if (file.getName().endsWith(".ref")) {
                    InputStream inputStream = new FileInputStream(file);
                    Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                    if (scanner.hasNext()) {
                        String target = scanner.next();
                        URL url = new URL("file", "", target);
                        libs.add(url);
                    }
                }
            }
        }
        return libs;
    }
}
