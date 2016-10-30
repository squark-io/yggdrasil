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
package io.squark.yggdrasil.logging.module;

import io.squark.nestedjarclassloader.NestedJarClassLoader;
import io.squark.yggdrasil.core.api.Constants;
import io.squark.yggdrasil.core.api.YggdrasilContext;
import io.squark.yggdrasil.core.api.logging.LogHelper;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.logging.api.CrappyLogger;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.AfterAll;
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

@SuppressWarnings("Duplicates")
public class LoggingModuleIntegrationTest {

    private static PrintStream originalOut = System.out;
    private static PrintStream originalErr = System.err;
    private static ByteArrayOutputStream combined = new ByteArrayOutputStream();
    private static ByteArrayOutputStream out = new ByteArrayOutputStream() {
        @Override
        public synchronized void write(byte[] b, int off, int len) {
            combined.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            combined.write(b);
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

    @AfterAll
    public static void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void mainLazyLoadTest() throws Exception {
        System.setProperty(Constants.YGGDRASIL_LOG_LEVEL, "DEBUG");
        NestedJarClassLoader nestedJarClassLoader = new NestedJarClassLoader(null);
        URL[] urls = ((URLClassLoader) this.getClass().getClassLoader()).getURLs();

        List<URL> urlList = new ArrayList<>();
        for (URL url : urls) {
            if (!url.getFile().contains("slf4j-jdk14") && !url.getFile().contains("yggdrasil-logging-fallback") &&
                !url.getFile().contains("yggdrasil-logging-module")) {
                urlList.add(url);
            }
        }
        nestedJarClassLoader.addURLs(urlList.toArray(new URL[urlList.size()]));
        String libsUrl = getClass().getClassLoader().getResource("META-INF/yggdrasil-optional-lib").getFile();
        URL overrideLibraryPathURL =
            new File(new File(URI.create(libsUrl.substring(0, libsUrl.indexOf("!/")))).getParentFile(), "classes").toURI()
                .toURL();
        Class<?> yggdrasilContext = nestedJarClassLoader.loadClass(YggdrasilContext.class.getName());
        Method overrideLibraryPath = yggdrasilContext.getMethod("overrideLibraryPath", URL.class);
        overrideLibraryPath.invoke(null, overrideLibraryPathURL);
        initiateLogging(new YggdrasilConfiguration(), nestedJarClassLoader);
        Method loggerFactoryMethod =
            Class.forName(LoggerFactory.class.getName(), true, nestedJarClassLoader).getMethod("getLogger", Class.class);
        Object logger = loggerFactoryMethod.invoke(null, LoggingModuleIntegrationTest.class);

        Assertions.assertEquals(CrappyLogger.class.getName(), logger.getClass().getName());

        nestedJarClassLoader.addURLs(
            new File("target/yggdrasil-logging-module-" + System.getProperty("project.version") + ".jar").toURI().toURL());
        File testDependenciesFile = new File("target/test-dependencies");
        List<URL> testDependencies = new ArrayList<>();
        for (File dep : testDependenciesFile.listFiles()) {
            testDependencies.add(dep.toURI().toURL());
        }
        nestedJarClassLoader.addURLs(testDependencies.toArray(new URL[testDependencies.size()]));
        initiateLogging(new YggdrasilConfiguration(), nestedJarClassLoader);

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


    private void initiateLogging(YggdrasilConfiguration configuration, NestedJarClassLoader nestedJarClassLoader)
        throws Exception {
        Class<?> logHelper = nestedJarClassLoader.loadClass(LogHelper.class.getName(), true);
        Method initiateLogging = logHelper.getMethod("initiateLoggingWithConfigAsBytes", byte[].class, Object.class, URL.class);
        initiateLogging.invoke(null, SerializationUtils.serialize(configuration), nestedJarClassLoader, null);
    }
}
