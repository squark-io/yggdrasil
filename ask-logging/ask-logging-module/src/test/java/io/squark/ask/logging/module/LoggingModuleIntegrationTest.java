package io.squark.ask.logging.module;

import io.squark.ask.core.api.Constants;
import io.squark.ask.core.api.AskContext;
import io.squark.ask.core.api.logging.LogHelper;
import io.squark.ask.core.api.model.AskConfiguration;
import io.squark.ask.logging.api.CrappyLogger;
import io.squark.ask.nestedjarclassloader.NestedJarClassLoader;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-08-17.
 * Copyright 2016
 */
@SuppressWarnings("Duplicates")
public class LoggingModuleIntegrationTest {

    private static PrintStream originalOut = System.out;
    private static PrintStream originalErr = System.err;
    private static ByteArrayOutputStream combined = new ByteArrayOutputStream();
    private static ByteArrayOutputStream out = new ByteArrayOutputStream() {
        @Override
        public synchronized void write(byte[] b, int off, int len) {
            combined.write(b, off, len);
            originalOut.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            combined.write(b);
            originalOut.write(b);
        }
    };
    private static ByteArrayOutputStream err = new ByteArrayOutputStream() {
        @Override
        public synchronized void write(byte[] b, int off, int len) {
            combined.write(b, off, len);
            originalErr.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            combined.write(b);
            originalErr.write(b);
        }
    };

    @BeforeAll
    public static void setUp() {
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @Test
    public void mainLazyLoadTest() throws Exception {
        System.setProperty(Constants.ASK_LOG_LEVEL, "DEBUG");
        NestedJarClassLoader nestedJarClassLoader = new NestedJarClassLoader(null);
        URL[] urls = ((URLClassLoader) this.getClass().getClassLoader()).getURLs();

        List<URL> urlList = new ArrayList<>();
        for (URL url : urls) {
            if (!url.getFile().contains("slf4j-jdk14") && !url.getFile().contains("ask-logging-fallback") &&
                !url.getFile().contains("ask-logging-module")) {
                urlList.add(url);
            }
        }
        nestedJarClassLoader.addURLs(urlList.toArray(new URL[urlList.size()]));
        String libsUrl = getClass().getClassLoader().getResource("META-INF/ask-optional-lib").getFile();
        URL overrideLibraryPathURL =
            new File(new File(URI.create(libsUrl.substring(0, libsUrl.indexOf("!/")))).getParentFile(), "classes").toURI()
                .toURL();
        Class<?> askContext = nestedJarClassLoader.loadClass(AskContext.class.getName());
        Method overrideLibraryPath = askContext.getMethod("overrideLibraryPath", URL.class);
        overrideLibraryPath.invoke(null, overrideLibraryPathURL);
        initiateLogging(new AskConfiguration(), nestedJarClassLoader);
        Method loggerFactoryMethod =
            Class.forName(LoggerFactory.class.getName(), true, nestedJarClassLoader).getMethod("getLogger", Class.class);
        Object logger = loggerFactoryMethod.invoke(null, LoggingModuleIntegrationTest.class);

        Assertions.assertEquals(CrappyLogger.class.getName(), logger.getClass().getName());

        nestedJarClassLoader.addURLs(
            new File("target/ask-logging-module-" + System.getProperty("project.version") + ".jar").toURI().toURL());
        File testDependenciesFile = new File("target/test-dependencies");
        List<URL> testDependencies = new ArrayList<>();
        for (File dep : testDependenciesFile.listFiles()) {
            testDependencies.add(dep.toURI().toURL());
        }
        nestedJarClassLoader.addURLs(testDependencies.toArray(new URL[testDependencies.size()]));
        initiateLogging(new AskConfiguration(), nestedJarClassLoader);

        logger = loggerFactoryMethod.invoke(null, LoggingModuleIntegrationTest.class);
        Assertions.assertEquals("org.apache.logging.slf4j.Log4jLogger", logger.getClass().getName());
        assertOutputDoesNotContainSlf4jWarnings();
    }

    private void assertOutputDoesNotContainSlf4jWarnings() {
        String result = new String(combined.toByteArray());
        if (result.contains("Failed to load class \"org.slf4j.impl.StaticLoggerBinder\"")) {
            throw new AssertionError("Failed to find org.slf4j.impl.StaticLoggerBinder implementation");
        }
        if (result.contains("Class path contains multiple SLF4J bindings")) {
            throw new AssertionError("Found multiple SLF4J bindings");
        }
    }


    private void initiateLogging(AskConfiguration configuration, NestedJarClassLoader nestedJarClassLoader)
        throws Exception {
        Class<?> logHelper = nestedJarClassLoader.loadClass(LogHelper.class.getName(), true);
        Method initiateLogging = logHelper.getMethod("initiateLoggingWithConfigAsBytes", byte[].class, Object.class, URL.class);
        initiateLogging.invoke(null, SerializationUtils.serialize(configuration), nestedJarClassLoader, null);
    }
}