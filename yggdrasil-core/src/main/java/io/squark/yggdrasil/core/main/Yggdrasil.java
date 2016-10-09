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
package io.squark.yggdrasil.core.main;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.squark.yggdrasil.core.api.Constants;
import io.squark.yggdrasil.core.api.FrameworkProviderService;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.exception.MainClassLoadException;
import io.squark.yggdrasil.core.api.exception.PropertyLoadException;
import io.squark.yggdrasil.core.api.logging.LogHelper;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.core.api.util.ConfigurationSerializer;
import io.squark.yggdrasil.core.api.util.LibHelper;
import io.squark.yggdrasil.core.api.util.ReflectionUtil;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import io.squark.nestedjarclassloader.NestedJarClassLoader;
import io.squark.nestedjarclassloader.NestedJarURLStreamHandler;
import io.squark.nestedjarclassloader.exception.NestedJarClassLoaderException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Yggdrasil {

    private static Logger logger = InternalLoggerBinder.getLogger(Yggdrasil.class);

    static { //runs when the main class is loaded.
        //This is necessary for some modules when not using Log4J2:
        System.setProperty("org.jboss.logging.provider", "slf4j");
    }

    @SuppressWarnings("unused")
    public static void internalMain(String[] args) {
        logger.info("Initiating Yggdrasil");
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            LogHelper.initiateLogging(null, classLoader, null, false);
            YggdrasilConfiguration configuration = getConfiguration();
            NestedJarClassLoader isolatedClassLoader = initiate(classLoader, configuration);
            loadMainClass(isolatedClassLoader, configuration, args);
        } catch (YggdrasilException | NestedJarClassLoaderException e) {
            logger.error(null, e);
        }
    }

    static YggdrasilConfiguration getConfiguration() throws PropertyLoadException {

        InputStream inputStream;
        inputStream = Yggdrasil.class.getResourceAsStream(Constants.YAML_PROPERTIES_FILE);
        YggdrasilConfiguration yggdrasilConfiguration = null;
        if (inputStream == null) {
            inputStream = Yggdrasil.class.getResourceAsStream(Constants.JSON_PROPERTIES_FILE);
            if (inputStream != null) {
                logger.info("Found JSON configuration");
                Gson gson = new Gson();
                try {
                    yggdrasilConfiguration = gson.fromJson(new InputStreamReader(inputStream), YggdrasilConfiguration.class);
                } catch (JsonParseException e) {
                    throw new PropertyLoadException(e);
                }
            }
        } else {
            logger.info("Found YAML configuration");
            Yaml yaml = new Yaml();
            try {
                yggdrasilConfiguration = yaml.loadAs(inputStream, YggdrasilConfiguration.class);
            } catch (YAMLException e) {
                throw new PropertyLoadException(e);
            }
        }
        if (inputStream == null) {
            throw new PropertyLoadException(
                    "Failed to find " + Constants.YAML_PROPERTIES_FILE + " or " + Constants.JSON_PROPERTIES_FILE);
        } else if (yggdrasilConfiguration == null) {
            throw new PropertyLoadException("Unknown error. Failed to load properties");
        }

        return yggdrasilConfiguration;

    }

    private static void loadMainClass(ClassLoader forClassLoader, YggdrasilConfiguration configuration, String[] args) throws
            MainClassLoadException
    {
        //Load main class:
        if (StringUtils.isNotEmpty(configuration.getMainClass())) {
            logger.debug("Loading main class " + configuration.getMainClass());
            try {
                ReflectionUtil.invokeMethod("main", configuration.getMainClass(), null, new Object[]{args}, null, forClassLoader,
                        null);
                logger.debug("Main class loaded");
            } catch (Throwable e) {
                throw new MainClassLoadException(e);
            }
        }
    }

    public static NestedJarClassLoader initiate(ClassLoader classLoader, YggdrasilConfiguration configuration) throws
            NestedJarClassLoaderException
    {
        if (!(classLoader instanceof NestedJarClassLoader)) {
            classLoader = new NestedJarClassLoader(classLoader);
        }

        try {
            return ReflectionUtil.invokeMethod("initiate", Yggdrasil.class.getName(), null,
                    new Object[]{ConfigurationSerializer.serializeConfig(configuration), classLoader},
                    new Class[]{byte[].class, Object.class}, classLoader, NestedJarClassLoader.class);
        } catch (Throwable e) {
            throw new NestedJarClassLoaderException(e);
        }
    }

    @SuppressWarnings("unused")
    private static NestedJarClassLoader initiate(byte[] configurationBytes, Object helperClassLoader) throws YggdrasilException {

        Thread.currentThread().setContextClassLoader((ClassLoader) helperClassLoader);
        YggdrasilConfiguration configuration = ConfigurationSerializer.deserializeConfig(configurationBytes);

        URL libs[] = LibHelper.getLibs(Yggdrasil.class, Constants.LIB_PATH);
        URL classesJar = getClassesJar(configuration);

        NestedJarClassLoader isolatedClassLoader;
        try {
            isolatedClassLoader = new NestedJarClassLoader(null);
            isolatedClassLoader.addURLs(libs);
        } catch (IOException e) {
            throw new YggdrasilException(e);
        }
        try {
            //Initiate logging again in case we found logging module, but do it in the isolated classloader.
            ReflectionUtil.invokeMethod("initiateLoggingWithConfigAsBytes", LogHelper.class.getName(), null,
                    new Object[]{configurationBytes, isolatedClassLoader, classesJar},
                    new Class[]{byte[].class, Object.class, URL.class}, isolatedClassLoader, null);
        } catch (Throwable e) {
            throw new YggdrasilException(e);
        }

        Set<String> loadedLibs = new HashSet<>();
        for (URL url : libs) {
            loadedLibs.add(FilenameUtils.getName(url.getFile()));
        }

        RemoteDependencyLoader.loadDependencies(isolatedClassLoader, (NestedJarClassLoader) helperClassLoader, configuration,
                loadedLibs);

        try {
            //Initiate logging again in case we found logging module, but do it in the isolated classloader.
            ReflectionUtil.invokeMethod("initiateLoggingWithConfigAsBytes", LogHelper.class.getName(), null,
                    new Object[]{configurationBytes, isolatedClassLoader, classesJar, false},
                    new Class[]{byte[].class, Object.class, URL.class, boolean.class}, isolatedClassLoader, null);
        } catch (Throwable e) {
            throw new YggdrasilException(e);
        }

        FrameworkProviderService.loadProviders(isolatedClassLoader, configuration);
        logger.info("Yggdrasil initiated");
        return isolatedClassLoader;
    }

    private static URL getClassesJar(YggdrasilConfiguration configuration) throws YggdrasilException {
        try {
            File ownFile = new File(Yggdrasil.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            JarFile ownJar = new JarFile(ownFile);
            Enumeration<JarEntry> entries = ownJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(configuration.getClassesJar())) {
                    return new URL(null, "jar:" + ownFile.toURI().toString() + "!/" + entry.getName(),
                            new NestedJarURLStreamHandler());
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new YggdrasilException(e);
        }
        return null;
    }

}
