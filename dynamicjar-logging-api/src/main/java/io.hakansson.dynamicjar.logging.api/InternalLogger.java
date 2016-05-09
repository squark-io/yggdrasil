package io.hakansson.dynamicjar.logging.api;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Class.forName;

/**
 * Created by Erik Håkansson on 2016-05-09.
 * WirelessCar
 */
public class InternalLogger {

    private static final String DYNAMICJAR_LOG_LEVEL = "dynamicjar.logLevel";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static boolean loggingInitialized = false;

    private LogLevel logLevel = null;
    private String name;
    private Logger logger;
    private Method loggerFactoryMethod;
    private Class clazz;

    private InternalLogger(String name) {
        this.name = name;
    }

    private InternalLogger(Class<?> clazz) {
        this.name = clazz.getName();
        this.clazz = clazz;
    }

    public static InternalLogger getLogger(Class<?> clazz) {
        return new InternalLogger(clazz);
    }

    public static boolean isLoggingInitialized() {
        return loggingInitialized;
    }

    public static void setLoggingInitialized(boolean loggingInitialized) {
        InternalLogger.loggingInitialized = loggingInitialized;
    }

    public void log(LogLevel level, Throwable throwable) {
        log(level, null, throwable);
    }

    public void log(LogLevel level, String message) {
        log(level, message, null);
    }

    public void log(LogLevel level, @Nullable String message, @Nullable Throwable throwable) {
        checkIfLoggerAvailable();
        if (logger != null) {
            if (message == null) {
                message = "*";
            }
            switch (level) {
                case OFF:
                    return;
                case FATAL:
                case ERROR:
                    logger.error(message, throwable);
                    break;
                case WARN:
                    logger.warn(message, throwable);
                    break;
                case INFO:
                    logger.info(message, throwable);
                    break;
                case DEBUG:
                    logger.debug(message, throwable);
                    break;
                case TRACE:
                    logger.trace(message, throwable);
                    break;
                default:
                    logger.info(message, throwable);
            }
            return;
        }
        if (logLevel == null) {
            logLevel = LogLevel.INFO;
            if (System.getProperty(DYNAMICJAR_LOG_LEVEL) != null) {
                logLevel = LogLevel.valueOf(System.getProperty(DYNAMICJAR_LOG_LEVEL));
            }
        }
        if (level.intLevel() <= logLevel.intLevel()) {
            String combinedMessage = message != null ? message : "";
            if (throwable != null) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                throwable.printStackTrace(printWriter);
                combinedMessage = (message != null ? message + "\n" : "") + stringWriter.toString();
            }

            String identifier =
                dateFormat.format(new Date()) + " [" + Thread.currentThread().getName() + "] " + level.name() + " ["
                    + name + "]";

            System.out.println(identifier + " " + combinedMessage);
        }
    }

    private void checkIfLoggerAvailable() {
        if (isLoggingInitialized() && logger == null) {
            if (loggerFactoryMethod == null) {
                try {
                    ClassLoader classLoader;
                    if (this.clazz != null) {
                        classLoader = clazz.getClassLoader();
                    } else {
                        classLoader = Thread.currentThread().getContextClassLoader();
                    }
                    loggerFactoryMethod =
                        Class.forName(LoggerFactory.class.getName(), true, classLoader).getDeclaredMethod("getLogger", String.class);
                } catch (NoSuchMethodException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            try {
                logger = (Logger) loggerFactoryMethod.invoke(null, name);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if (logger instanceof NOPLogger) {
                logger = null;
            }
        }
    }
}
