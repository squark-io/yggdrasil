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
package io.squark.yggdrasil.logging.api;

import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-10-28.
 * Copyright 2016
 */
public class CrappyLoggerTest {

    private CrappyLogger crappyLogger;
    private Logger mock = Mockito.mock(Logger.class);


    @Before
    public void setup() {
        System.setProperty(CrappyLogger.YGGDRASIL_LOG_LEVEL, LogLevel.TRACE.name());
        crappyLogger = Mockito.spy(new CrappyLoggerTestImpl(CrappyLoggerTest.class.getName()));
    }

    @After
    public void tearDown() {
        System.clearProperty(CrappyLogger.YGGDRASIL_LOG_LEVEL);
    }

    @Test
    public void isReplaced() throws Exception {
        crappyLogger.setDelegate(mock);
        Assert.assertTrue(crappyLogger.isReplaced());
    }

    @Test
    public void getName() throws Exception {
        Assert.assertEquals(CrappyLoggerTest.class.getName(), crappyLogger.getName());
    }

    @Test
    public void isTraceEnabled() throws Exception {
        Assert.assertTrue(crappyLogger.isTraceEnabled());
        Marker marker = MarkerFactory.getMarker("test");
        Assert.assertTrue(crappyLogger.isTraceEnabled(marker));

        crappyLogger.setDelegate(mock);

        crappyLogger.isTraceEnabled(marker);
        Mockito.verify(mock).isTraceEnabled(marker);
    }

    @Test
    public void trace() throws Exception {
        crappyLogger.trace("test");
        verifyLog(LogLevel.TRACE, "test", null);
        crappyLogger.trace("%s", "test");
        verifyLog(LogLevel.TRACE, String.format("%s", "test"), null);
        crappyLogger.trace("%s %s", "test", "test2");
        verifyLog(LogLevel.TRACE, String.format("%s %s", "test", "test2"), null);
        crappyLogger.trace("%s %s %s", "test", "test2", "test3");
        verifyLog(LogLevel.TRACE, String.format("%s %s %s", "test", "test2", "test3"), null);
        Throwable aThrowable = new Throwable();
        crappyLogger.trace("test", aThrowable);
        verifyLog(LogLevel.TRACE, "test", aThrowable);

        Marker marker = MarkerFactory.getMarker("test");
        crappyLogger.trace(marker, "test");
        verifyLog(LogLevel.TRACE, "test", null);
        crappyLogger.trace(marker, "%s", "test");
        verifyLog(LogLevel.TRACE, String.format("%s", "test"), null);
        crappyLogger.trace(marker, "%s %s", "test", "test2");
        verifyLog(LogLevel.TRACE, String.format("%s %s", "test", "test2"), null);
        crappyLogger.trace(marker, "%s %s %s", "test", "test2", "test3");
        verifyLog(LogLevel.TRACE, String.format("%s %s %s", "test", "test2", "test3"), null);
        crappyLogger.trace(marker, "test", aThrowable);
        verifyLog(LogLevel.TRACE, "test", aThrowable);


        crappyLogger.setDelegate(mock);

        crappyLogger.trace("test");
        verifyMock("trace", "test");
        crappyLogger.trace("%s", "test");
        verifyMock("trace", new Object[]{"%s", "test"}, new Class[]{String.class, Object.class});
        crappyLogger.trace("%s %s", "test", "test2");
        verifyMock("trace", new Object[]{"%s %s", "test", "test2"}, new Class[]{String.class, Object.class, Object.class});
        crappyLogger.trace("%s %s %s", "test", "test2", "test3");
        verifyMock("trace", new Object[]{"%s %s %s", new Object[]{"test", "test2", "test3"}},
            new Class[]{String.class, Object[].class});
        crappyLogger.trace("test", aThrowable);
        verifyMock("trace", "test", aThrowable);

        crappyLogger.trace(marker, "test");
        verifyMock("trace", new Object[]{marker, "test"}, new Class[]{Marker.class, String.class});
        crappyLogger.trace(marker, "%s", "test");
        verifyMock("trace", new Object[]{marker, "%s", "test"}, new Class[]{Marker.class, String.class, Object.class});
        crappyLogger.trace(marker, "%s %s", "test", "test2");
        verifyMock("trace", new Object[]{marker, "%s %s", "test", "test2"},
            new Class[]{Marker.class, String.class, Object.class, Object.class});
        crappyLogger.trace(marker, "%s %s %s", "test", "test2", "test3");
        verifyMock("trace", new Object[]{marker, "%s %s %s", new Object[]{"test", "test2", "test3"}},
            new Class[]{Marker.class, String.class, Object[].class});
        crappyLogger.trace(marker, "test", aThrowable);
        verifyMock("trace", new Object[]{marker, "test", aThrowable}, new Class[]{Marker.class, String.class, Throwable.class});
    }

    @Test
    public void isDebugEnabled() throws Exception {
        Assert.assertTrue(crappyLogger.isDebugEnabled());
        Marker marker = MarkerFactory.getMarker("test");
        Assert.assertTrue(crappyLogger.isDebugEnabled(marker));

        crappyLogger.setDelegate(mock);

        crappyLogger.isDebugEnabled(marker);
        Mockito.verify(mock).isDebugEnabled(marker);
    }

    @Test
    public void debug() throws Exception {
        crappyLogger.debug("test");
        verifyLog(LogLevel.DEBUG, "test", null);
        crappyLogger.debug("%s", "test");
        verifyLog(LogLevel.DEBUG, String.format("%s", "test"), null);
        crappyLogger.debug("%s %s", "test", "test2");
        verifyLog(LogLevel.DEBUG, String.format("%s %s", "test", "test2"), null);
        crappyLogger.debug("%s %s %s", "test", "test2", "test3");
        verifyLog(LogLevel.DEBUG, String.format("%s %s %s", "test", "test2", "test3"), null);
        Throwable aThrowable = new Throwable();
        crappyLogger.debug("test", aThrowable);
        verifyLog(LogLevel.DEBUG, "test", aThrowable);

        Marker marker = MarkerFactory.getMarker("test");
        crappyLogger.debug(marker, "test");
        verifyLog(LogLevel.DEBUG, "test", null);
        crappyLogger.debug(marker, "%s", "test");
        verifyLog(LogLevel.DEBUG, String.format("%s", "test"), null);
        crappyLogger.debug(marker, "%s %s", "test", "test2");
        verifyLog(LogLevel.DEBUG, String.format("%s %s", "test", "test2"), null);
        crappyLogger.debug(marker, "%s %s %s", "test", "test2", "test3");
        verifyLog(LogLevel.DEBUG, String.format("%s %s %s", "test", "test2", "test3"), null);
        crappyLogger.debug(marker, "test", aThrowable);
        verifyLog(LogLevel.DEBUG, "test", aThrowable);


        crappyLogger.setDelegate(mock);

        crappyLogger.debug("test");
        verifyMock("debug", "test");
        crappyLogger.debug("%s", "test");
        verifyMock("debug", new Object[]{"%s", "test"}, new Class[]{String.class, Object.class});
        crappyLogger.debug("%s %s", "test", "test2");
        verifyMock("debug", new Object[]{"%s %s", "test", "test2"}, new Class[]{String.class, Object.class, Object.class});
        crappyLogger.debug("%s %s %s", "test", "test2", "test3");
        verifyMock("debug", new Object[]{"%s %s %s", new Object[]{"test", "test2", "test3"}},
            new Class[]{String.class, Object[].class});
        crappyLogger.debug("test", aThrowable);
        verifyMock("debug", "test", aThrowable);

        crappyLogger.debug(marker, "test");
        verifyMock("debug", new Object[]{marker, "test"}, new Class[]{Marker.class, String.class});
        crappyLogger.debug(marker, "%s", "test");
        verifyMock("debug", new Object[]{marker, "%s", "test"}, new Class[]{Marker.class, String.class, Object.class});
        crappyLogger.debug(marker, "%s %s", "test", "test2");
        verifyMock("debug", new Object[]{marker, "%s %s", "test", "test2"},
            new Class[]{Marker.class, String.class, Object.class, Object.class});
        crappyLogger.debug(marker, "%s %s %s", "test", "test2", "test3");
        verifyMock("debug", new Object[]{marker, "%s %s %s", new Object[]{"test", "test2", "test3"}},
            new Class[]{Marker.class, String.class, Object[].class});
        crappyLogger.debug(marker, "test", aThrowable);
        verifyMock("debug", new Object[]{marker, "test", aThrowable}, new Class[]{Marker.class, String.class, Throwable.class});
    }

    @Test
    public void isInfoEnabled() throws Exception {
        Assert.assertTrue(crappyLogger.isInfoEnabled());
        Marker marker = MarkerFactory.getMarker("test");
        Assert.assertTrue(crappyLogger.isInfoEnabled(marker));

        crappyLogger.setDelegate(mock);

        crappyLogger.isInfoEnabled(marker);
        Mockito.verify(mock).isInfoEnabled(marker);
    }

    @Test
    public void info() throws Exception {
        crappyLogger.info("test");
        verifyLog(LogLevel.INFO, "test", null);
        crappyLogger.info("%s", "test");
        verifyLog(LogLevel.INFO, String.format("%s", "test"), null);
        crappyLogger.info("%s %s", "test", "test2");
        verifyLog(LogLevel.INFO, String.format("%s %s", "test", "test2"), null);
        crappyLogger.info("%s %s %s", "test", "test2", "test3");
        verifyLog(LogLevel.INFO, String.format("%s %s %s", "test", "test2", "test3"), null);
        Throwable aThrowable = new Throwable();
        crappyLogger.info("test", aThrowable);
        verifyLog(LogLevel.INFO, "test", aThrowable);

        Marker marker = MarkerFactory.getMarker("test");
        crappyLogger.info(marker, "test");
        verifyLog(LogLevel.INFO, "test", null);
        crappyLogger.info(marker, "%s", "test");
        verifyLog(LogLevel.INFO, String.format("%s", "test"), null);
        crappyLogger.info(marker, "%s %s", "test", "test2");
        verifyLog(LogLevel.INFO, String.format("%s %s", "test", "test2"), null);
        crappyLogger.info(marker, "%s %s %s", "test", "test2", "test3");
        verifyLog(LogLevel.INFO, String.format("%s %s %s", "test", "test2", "test3"), null);
        crappyLogger.info(marker, "test", aThrowable);
        verifyLog(LogLevel.INFO, "test", aThrowable);


        crappyLogger.setDelegate(mock);

        crappyLogger.info("test");
        verifyMock("info", "test");
        crappyLogger.info("%s", "test");
        verifyMock("info", new Object[]{"%s", "test"}, new Class[]{String.class, Object.class});
        crappyLogger.info("%s %s", "test", "test2");
        verifyMock("info", new Object[]{"%s %s", "test", "test2"}, new Class[]{String.class, Object.class, Object.class});
        crappyLogger.info("%s %s %s", "test", "test2", "test3");
        verifyMock("info", new Object[]{"%s %s %s", new Object[]{"test", "test2", "test3"}},
            new Class[]{String.class, Object[].class});
        crappyLogger.info("test", aThrowable);
        verifyMock("info", "test", aThrowable);

        crappyLogger.info(marker, "test");
        verifyMock("info", new Object[]{marker, "test"}, new Class[]{Marker.class, String.class});
        crappyLogger.info(marker, "%s", "test");
        verifyMock("info", new Object[]{marker, "%s", "test"}, new Class[]{Marker.class, String.class, Object.class});
        crappyLogger.info(marker, "%s %s", "test", "test2");
        verifyMock("info", new Object[]{marker, "%s %s", "test", "test2"},
            new Class[]{Marker.class, String.class, Object.class, Object.class});
        crappyLogger.info(marker, "%s %s %s", "test", "test2", "test3");
        verifyMock("info", new Object[]{marker, "%s %s %s", new Object[]{"test", "test2", "test3"}},
            new Class[]{Marker.class, String.class, Object[].class});
        crappyLogger.info(marker, "test", aThrowable);
        verifyMock("info", new Object[]{marker, "test", aThrowable}, new Class[]{Marker.class, String.class, Throwable.class});
    }

    @Test
    public void isWarnEnabled() throws Exception {
        Assert.assertTrue(crappyLogger.isWarnEnabled());
        Marker marker = MarkerFactory.getMarker("test");
        Assert.assertTrue(crappyLogger.isWarnEnabled(marker));

        crappyLogger.setDelegate(mock);

        crappyLogger.isWarnEnabled(marker);
        Mockito.verify(mock).isWarnEnabled(marker);
    }

    @Test
    public void warn() throws Exception {
        crappyLogger.warn("test");
        verifyLog(LogLevel.WARN, "test", null);
        crappyLogger.warn("%s", "test");
        verifyLog(LogLevel.WARN, String.format("%s", "test"), null);
        crappyLogger.warn("%s %s", "test", "test2");
        verifyLog(LogLevel.WARN, String.format("%s %s", "test", "test2"), null);
        crappyLogger.warn("%s %s %s", "test", "test2", "test3");
        verifyLog(LogLevel.WARN, String.format("%s %s %s", "test", "test2", "test3"), null);
        Throwable aThrowable = new Throwable();
        crappyLogger.warn("test", aThrowable);
        verifyLog(LogLevel.WARN, "test", aThrowable);

        Marker marker = MarkerFactory.getMarker("test");
        crappyLogger.warn(marker, "test");
        verifyLog(LogLevel.WARN, "test", null);
        crappyLogger.warn(marker, "%s", "test");
        verifyLog(LogLevel.WARN, String.format("%s", "test"), null);
        crappyLogger.warn(marker, "%s %s", "test", "test2");
        verifyLog(LogLevel.WARN, String.format("%s %s", "test", "test2"), null);
        crappyLogger.warn(marker, "%s %s %s", "test", "test2", "test3");
        verifyLog(LogLevel.WARN, String.format("%s %s %s", "test", "test2", "test3"), null);
        crappyLogger.warn(marker, "test", aThrowable);
        verifyLog(LogLevel.WARN, "test", aThrowable);


        crappyLogger.setDelegate(mock);

        crappyLogger.warn("test");
        verifyMock("warn", "test");
        crappyLogger.warn("%s", "test");
        verifyMock("warn", new Object[]{"%s", "test"}, new Class[]{String.class, Object.class});
        crappyLogger.warn("%s %s", "test", "test2");
        verifyMock("warn", new Object[]{"%s %s", "test", "test2"}, new Class[]{String.class, Object.class, Object.class});
        crappyLogger.warn("%s %s %s", "test", "test2", "test3");
        verifyMock("warn", new Object[]{"%s %s %s", new Object[]{"test", "test2", "test3"}},
            new Class[]{String.class, Object[].class});
        crappyLogger.warn("test", aThrowable);
        verifyMock("warn", "test", aThrowable);

        crappyLogger.warn(marker, "test");
        verifyMock("warn", new Object[]{marker, "test"}, new Class[]{Marker.class, String.class});
        crappyLogger.warn(marker, "%s", "test");
        verifyMock("warn", new Object[]{marker, "%s", "test"}, new Class[]{Marker.class, String.class, Object.class});
        crappyLogger.warn(marker, "%s %s", "test", "test2");
        verifyMock("warn", new Object[]{marker, "%s %s", "test", "test2"},
            new Class[]{Marker.class, String.class, Object.class, Object.class});
        crappyLogger.warn(marker, "%s %s %s", "test", "test2", "test3");
        verifyMock("warn", new Object[]{marker, "%s %s %s", new Object[]{"test", "test2", "test3"}},
            new Class[]{Marker.class, String.class, Object[].class});
        crappyLogger.warn(marker, "test", aThrowable);
        verifyMock("warn", new Object[]{marker, "test", aThrowable}, new Class[]{Marker.class, String.class, Throwable.class});
    }

    @Test
    public void isErrorEnabled() throws Exception {
        Assert.assertTrue(crappyLogger.isErrorEnabled());
        Marker marker = MarkerFactory.getMarker("test");
        Assert.assertTrue(crappyLogger.isErrorEnabled(marker));

        crappyLogger.setDelegate(mock);

        crappyLogger.isErrorEnabled(marker);
        Mockito.verify(mock).isErrorEnabled(marker);
    }

    @Test
    public void error() throws Exception {
        crappyLogger.error("test");
        verifyLog(LogLevel.ERROR, "test", null);
        crappyLogger.error("%s", "test");
        verifyLog(LogLevel.ERROR, String.format("%s", "test"), null);
        crappyLogger.error("%s %s", "test", "test2");
        verifyLog(LogLevel.ERROR, String.format("%s %s", "test", "test2"), null);
        crappyLogger.error("%s %s %s", "test", "test2", "test3");
        verifyLog(LogLevel.ERROR, String.format("%s %s %s", "test", "test2", "test3"), null);
        Throwable aThrowable = new Throwable();
        crappyLogger.error("test", aThrowable);
        verifyLog(LogLevel.ERROR, "test", aThrowable);

        Marker marker = MarkerFactory.getMarker("test");
        crappyLogger.error(marker, "test");
        verifyLog(LogLevel.ERROR, "test", null);
        crappyLogger.error(marker, "%s", "test");
        verifyLog(LogLevel.ERROR, String.format("%s", "test"), null);
        crappyLogger.error(marker, "%s %s", "test", "test2");
        verifyLog(LogLevel.ERROR, String.format("%s %s", "test", "test2"), null);
        crappyLogger.error(marker, "%s %s %s", "test", "test2", "test3");
        verifyLog(LogLevel.ERROR, String.format("%s %s %s", "test", "test2", "test3"), null);
        crappyLogger.error(marker, "test", aThrowable);
        verifyLog(LogLevel.ERROR, "test", aThrowable);


        crappyLogger.setDelegate(mock);

        crappyLogger.error("test");
        verifyMock("error", "test");
        crappyLogger.error("%s", "test");
        verifyMock("error", new Object[]{"%s", "test"}, new Class[]{String.class, Object.class});
        crappyLogger.error("%s %s", "test", "test2");
        verifyMock("error", new Object[]{"%s %s", "test", "test2"}, new Class[]{String.class, Object.class, Object.class});
        crappyLogger.error("%s %s %s", "test", "test2", "test3");
        verifyMock("error", new Object[]{"%s %s %s", new Object[]{"test", "test2", "test3"}},
            new Class[]{String.class, Object[].class});
        crappyLogger.error("test", aThrowable);
        verifyMock("error", "test", aThrowable);

        crappyLogger.error(marker, "test");
        verifyMock("error", new Object[]{marker, "test"}, new Class[]{Marker.class, String.class});
        crappyLogger.error(marker, "%s", "test");
        verifyMock("error", new Object[]{marker, "%s", "test"}, new Class[]{Marker.class, String.class, Object.class});
        crappyLogger.error(marker, "%s %s", "test", "test2");
        verifyMock("error", new Object[]{marker, "%s %s", "test", "test2"},
            new Class[]{Marker.class, String.class, Object.class, Object.class});
        crappyLogger.error(marker, "%s %s %s", "test", "test2", "test3");
        verifyMock("error", new Object[]{marker, "%s %s %s", new Object[]{"test", "test2", "test3"}},
            new Class[]{Marker.class, String.class, Object[].class});
        crappyLogger.error(marker, "test", aThrowable);
        verifyMock("error", new Object[]{marker, "test", aThrowable}, new Class[]{Marker.class, String.class, Throwable.class});
    }

    @Test
    public void logTest() {
        CrappyLogger real = new CrappyLogger(CrappyLoggerTest.class.getName());
        PrintStream old = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                //doNothing
            }
        }));
        real.log(null, "test", new Exception());
        System.setOut(old);
    }

    private void verifyLog(LogLevel logLevel, String message, Throwable throwable) {
        Mockito.verify(crappyLogger).log(logLevel, message, throwable);
        Mockito.reset(crappyLogger);
    }

    private void verifyMock(String methodName, Object[] args, Class[] types) throws Exception {
        Method method = Logger.class.getMethod(methodName, types);
        method.invoke(Mockito.verify(mock), args);
        Mockito.reset(mock);
    }

    private void verifyMock(String methodName, Object... args) throws Exception {
        Class[] types = Arrays.stream(args).map((Function<Object, Class>) Object::getClass).collect(Collectors.toList())
            .toArray(new Class[args.length]);
        verifyMock(methodName, args, types);
    }

    private class CrappyLoggerTestImpl extends CrappyLogger {

        CrappyLoggerTestImpl(String name) {
            super(name);
        }

        @Override
        void log(LogLevel level, @Nullable String message, @Nullable Throwable throwable) {
            //do nothing
        }
    }

}