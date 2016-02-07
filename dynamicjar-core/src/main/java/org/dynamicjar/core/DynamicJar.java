package org.dynamicjar.core;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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


        String userHome = System.getProperty("user.home");
        String mavenHome =
            isNotEmpty(System.getProperty("M2_HOME")) ? System.getProperty("M2_HOME") :
            isNotEmpty(userHome) ? userHome + "/.m2/" : "./.m2/";

        logger.debug("Maven home: " + mavenHome);
        try {
            List<File> dependencyFiles =
                MavenHelper.getDependencyFiles(pomInputStream, new File(mavenHome + "/repository"));
            logger.info(dependencyFiles.toString());
        } catch (DependencyCollectionException e) {
            e.printStackTrace();
        } catch (DependencyResolutionException e) {
            e.printStackTrace();
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
