package org.dynamicjar.core.main;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dynamicjar.core.exception.DependencyResolutionException;
import org.dynamicjar.core.model.DependencyTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * *** AUTOTRADE ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-04.
 * Copyright 2016
 */
public class DynamicJar {

    private static Logger logger = LoggerFactory.getLogger(DynamicJar.class);

    private DynamicJar() {
        //Disallow instantiation
    }

    public static void loadDependencies(Class mainClass, String groupId, String artifactId)
        throws IOException, XmlPullParserException {

        String path = "/META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml";
        URL pomUrl =
            mainClass.getResource("/META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml");

        InputStream pomInputStream = mainClass.getResourceAsStream(path);

        try {
            DependencyTreeNode dependencies = MavenHelper.getDependencyFiles(pomInputStream);
            logger.debug(dependencies.toString());
        } catch (DependencyResolutionException e) {
            logger.error("Failed to resolve dependencies", e);
        }
    }


    public static void addJar(String jar) throws IOException {
        File f = new File(jar);
        addJar(f);
    }

    public static void addJar(File jar) throws IOException {
        addJar(jar.toURI().toURL());
    }

    public static void addJar(URL jar) throws IOException {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<?> urlClassLoaderClass = URLClassLoader.class;

        try {
            Method method = urlClassLoaderClass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(systemClassLoader, jar);
        } catch (Throwable t) {
            logger.error("Failed to load JAR", t);
            throw new IOException(t);
        }

    }


}
