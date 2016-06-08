package io.hakansson.dynamicjar.logging.api;

import org.jetbrains.annotations.Nullable;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-07.
 * Copyright 2016
 */
public final class CrappyLoggerFactory implements ILoggerFactory {

    private static final String DYNAMICJAR_LOG_LEVEL = "dynamicjar.logLevel";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private ReplaceableLoggerBinder replaceableLoggerBinder;

    public CrappyLoggerFactory(ReplaceableLoggerBinder staticLoggerBinder) {
        replaceableLoggerBinder = staticLoggerBinder;
    }

    @Override
    public Logger getLogger(String name) {
        CrappyLogger logger = new CrappyLogger(name);
        replaceableLoggerBinder.register(logger);
        return logger;
    }

    public class CrappyLogger implements Logger {

        private String name;
        private LogLevel logLevel;
        private Logger delegate;

        public CrappyLogger(String name) {
            this.name = name;
            logLevel = LogLevel.INFO;
            if (System.getProperty(DYNAMICJAR_LOG_LEVEL) != null) {
                logLevel = LogLevel.valueOf(System.getProperty(DYNAMICJAR_LOG_LEVEL));
            }
        }

        public Logger getDelegate() {
            return delegate;
        }

        public void setDelegate(Logger delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getName() {
            if (delegate != null) {
                return delegate.getName();
            }
            return name;
        }

        @Override
        public boolean isTraceEnabled() {
            if (delegate != null) {
                return delegate.isTraceEnabled();
            }
            return (logLevel.intLevel() >= LogLevel.TRACE.intLevel());
        }

        @Override
        public void trace(String msg) {
            if (delegate != null) {
                delegate.trace(msg);
                return;
            }
            log(LogLevel.TRACE, msg, null);
        }

        @Override
        public void trace(String format, Object arg) {
            if (delegate != null) {
                delegate.trace(format, arg);
                return;
            }
            log(LogLevel.TRACE, String.format(format, arg), null);
        }

        @Override
        public void trace(String format, Object arg1, Object arg2) {
            if (delegate != null) {
                delegate.trace(format, arg1, arg2);
                return;
            }
            log(LogLevel.TRACE, String.format(format, arg1, arg2), null);
        }

        @Override
        public void trace(String format, Object... arguments) {
            if (delegate != null) {
                delegate.trace(format, arguments);
                return;
            }
            log(LogLevel.TRACE, String.format(format, arguments), null);
        }

        @Override
        public void trace(String msg, Throwable t) {
            if (delegate != null) {
                delegate.trace(msg, t);
                return;
            }
            log(LogLevel.TRACE, null, t);
        }

        @Override
        public boolean isTraceEnabled(Marker marker) {
            if (delegate != null) {
                return delegate.isTraceEnabled(marker);
            }
            return isTraceEnabled();
        }

        @Override
        public void trace(Marker marker, String msg) {
            if (delegate != null) {
                delegate.trace(marker, msg);
                return;
            }
            trace(msg);
        }

        @Override
        public void trace(Marker marker, String format, Object arg) {
            if (delegate != null) {
                delegate.trace(marker, format, arg);
                return;
            }
            trace(format, arg);
        }

        @Override
        public void trace(Marker marker, String format, Object arg1, Object arg2) {
            if (delegate != null) {
                delegate.trace(marker, format, arg1, arg2);
                return;
            }
            trace(format, arg1, arg2);
        }

        @Override
        public void trace(Marker marker, String format, Object... argArray) {
            if (delegate != null) {
                delegate.trace(marker, format, argArray);
                return;
            }
            trace(format, argArray);
        }

        @Override
        public void trace(Marker marker, String msg, Throwable t) {
            if (delegate != null) {
                delegate.trace(marker, msg, t);
                return;
            }
            trace(msg, t);
        }

        @Override
        public boolean isDebugEnabled() {
            if (delegate != null) {
                return delegate.isDebugEnabled();
            }
            return (logLevel.intLevel() >= LogLevel.DEBUG.intLevel());
        }

        @Override
        public void debug(String msg) {
            if (delegate != null) {
                delegate.debug(msg);
                return;
            }
            log(LogLevel.DEBUG, msg, null);
        }

        @Override
        public void debug(String format, Object arg) {
            if (delegate != null) {
                delegate.debug(format, arg);
                return;
            }
            log(LogLevel.DEBUG, String.format(format, arg), null);
        }

        @Override
        public void debug(String format, Object arg1, Object arg2) {
            if (delegate != null) {
                delegate.debug(format, arg1, arg2);
                return;
            }
            log(LogLevel.DEBUG, String.format(format, arg1, arg2), null);
        }

        @Override
        public void debug(String format, Object... arguments) {
            if (delegate != null) {
                delegate.debug(format, arguments);
                return;
            }
            log(LogLevel.DEBUG, String.format(format, arguments), null);
        }

        @Override
        public void debug(String msg, Throwable t) {
            if (delegate != null) {
                delegate.debug(msg, t);
                return;
            }
            log(LogLevel.DEBUG, msg, t);
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            if (delegate != null) {
                return delegate.isDebugEnabled(marker);
            }
            return isDebugEnabled();
        }

        @Override
        public void debug(Marker marker, String msg) {
            if (delegate != null) {
                delegate.debug(marker, msg);
                return;
            }
            debug(msg);
        }

        @Override
        public void debug(Marker marker, String format, Object arg) {
            if (delegate != null) {
                delegate.debug(marker, format, arg);
                return;
            }
            debug(format, arg);
        }

        @Override
        public void debug(Marker marker, String format, Object arg1, Object arg2) {
            if (delegate != null) {
                delegate.debug(marker, format, arg1, arg2);
                return;
            }
            debug(format, arg1, arg2);
        }

        @Override
        public void debug(Marker marker, String format, Object... arguments) {
            if (delegate != null) {
                delegate.debug(marker, format, arguments);
                return;
            }
            debug(format, arguments);
        }

        @Override
        public void debug(Marker marker, String msg, Throwable t) {
            if (delegate != null) {
                delegate.debug(marker, msg, t);
                return;
            }
            debug(msg, t);
        }

        @Override
        public boolean isInfoEnabled() {
            if (delegate != null) {
                return delegate.isInfoEnabled();
            }
            return (logLevel.intLevel() >= LogLevel.INFO.intLevel());
        }

        @Override
        public void info(String msg) {
            if (delegate != null) {
                delegate.info(msg);
                return;
            }
            log(LogLevel.INFO, msg, null);
        }

        @Override
        public void info(String format, Object arg) {
            if (delegate != null) {
                delegate.info(format, arg);
                return;
            }
            log(LogLevel.INFO, String.format(format, arg), null);
        }

        @Override
        public void info(String format, Object arg1, Object arg2) {
            if (delegate != null) {
                delegate.info(format, arg1, arg2);
                return;
            }
            log(LogLevel.INFO, String.format(format, arg1, arg2), null);
        }

        @Override
        public void info(String format, Object... arguments) {
            if (delegate != null) {
                delegate.info(format, arguments);
                return;
            }
            log(LogLevel.INFO, String.format(format, arguments), null);
        }

        @Override
        public void info(String msg, Throwable t) {
            if (delegate != null) {
                delegate.info(msg, t);
                return;
            }
            log(LogLevel.INFO, msg, t);
        }

        @Override
        public boolean isInfoEnabled(Marker marker) {
            if (delegate != null) {
                return delegate.isInfoEnabled(marker);
            }
            return isInfoEnabled();
        }

        @Override
        public void info(Marker marker, String msg) {
            if (delegate != null) {
                delegate.info(marker, msg);
                return;
            }
            info(msg);
        }

        @Override
        public void info(Marker marker, String format, Object arg) {
            if (delegate != null) {
                delegate.info(marker, format, arg);
                return;
            }
            info(format, arg);
        }

        @Override
        public void info(Marker marker, String format, Object arg1, Object arg2) {
            if (delegate != null) {
                delegate.info(marker, format, arg1, arg2);
                return;
            }
            info(format, arg1, arg2);
        }

        @Override
        public void info(Marker marker, String format, Object... arguments) {
            if (delegate != null) {
                delegate.info(marker, format, arguments);
                return;
            }
            info(format, arguments);
        }

        @Override
        public void info(Marker marker, String msg, Throwable t) {
            if (delegate != null) {
                delegate.info(marker, msg, t);
                return;
            }
            info(msg, t);
        }

        @Override
        public boolean isWarnEnabled() {
            if (delegate != null) {
                return delegate.isWarnEnabled();
            }
            return (logLevel.intLevel() >= LogLevel.WARN.intLevel());
        }

        @Override
        public void warn(String msg) {
            if (delegate != null) {
                delegate.warn(msg);
                return;
            }
            log(LogLevel.WARN, msg, null);
        }

        @Override
        public void warn(String format, Object arg) {
            if (delegate != null) {
                delegate.warn(format, arg);
                return;
            }
            log(LogLevel.WARN, String.format(format, arg), null);
        }

        @Override
        public void warn(String format, Object... arguments) {
            if (delegate != null) {
                delegate.warn(format, arguments);
                return;
            }
            log(LogLevel.WARN, String.format(format, arguments), null);
        }

        @Override
        public void warn(String format, Object arg1, Object arg2) {
            if (delegate != null) {
                delegate.warn(format, arg1, arg2);
                return;
            }
            log(LogLevel.WARN, String.format(format, arg1, arg2), null);
        }

        @Override
        public void warn(String msg, Throwable t) {
            if (delegate != null) {
                delegate.warn(msg, t);
                return;
            }
            log(LogLevel.WARN, msg, t);
        }

        @Override
        public boolean isWarnEnabled(Marker marker) {
            if (delegate != null) {
                return delegate.isWarnEnabled(marker);
            }
            return isWarnEnabled();
        }

        @Override
        public void warn(Marker marker, String msg) {
            if (delegate != null) {
                delegate.warn(marker, msg);
                return;
            }
            warn(msg);
        }

        @Override
        public void warn(Marker marker, String format, Object arg) {
            if (delegate != null) {
                delegate.warn(marker, format, arg);
                return;
            }
            warn(format, arg);
        }

        @Override
        public void warn(Marker marker, String format, Object arg1, Object arg2) {
            if (delegate != null) {
                delegate.warn(marker, format, arg1, arg2);
                return;
            }
            warn(format, arg1, arg2);
        }

        @Override
        public void warn(Marker marker, String format, Object... arguments) {
            if (delegate != null) {
                delegate.warn(marker, format, arguments);
                return;
            }
            warn(format, arguments);
        }

        @Override
        public void warn(Marker marker, String msg, Throwable t) {
            if (delegate != null) {
                delegate.warn(marker, msg, t);
                return;
            }
            warn(msg, t);
        }

        @Override
        public boolean isErrorEnabled() {
            if (delegate != null) {
                return delegate.isErrorEnabled();
            }
            return (logLevel.intLevel() >= LogLevel.ERROR.intLevel());
        }

        @Override
        public void error(String msg) {
            if (delegate != null) {
                delegate.error(msg);
                return;
            }
            log(LogLevel.ERROR, msg, null);
        }

        @Override
        public void error(String format, Object arg) {
            if (delegate != null) {
                delegate.error(format, arg);
                return;
            }
            log(LogLevel.ERROR, String.format(format, arg), null);
        }

        @Override
        public void error(String format, Object arg1, Object arg2) {
            if (delegate != null) {
                delegate.error(format, arg1, arg2);
                return;
            }
            log(LogLevel.ERROR, String.format(format, arg1, arg2), null);
        }

        @Override
        public void error(String format, Object... arguments) {
            if (delegate != null) {
                delegate.error(format, arguments);
                return;
            }
            log(LogLevel.ERROR, String.format(format, arguments), null);
        }

        @Override
        public void error(String msg, Throwable t) {
            if (delegate != null) {
                delegate.error(msg, t);
                return;
            }
            log(LogLevel.ERROR, msg, t);
        }

        @Override
        public boolean isErrorEnabled(Marker marker) {
            if (delegate != null) {
                return delegate.isErrorEnabled(marker);
            }
            return isErrorEnabled();
        }

        @Override
        public void error(Marker marker, String msg) {
            if (delegate != null) {
                delegate.error(marker, msg);
                return;
            }
            error(msg);
        }

        @Override
        public void error(Marker marker, String format, Object arg) {
            if (delegate != null) {
                delegate.error(marker, format, arg);
                return;
            }
            error(format, arg);
        }

        @Override
        public void error(Marker marker, String format, Object arg1, Object arg2) {
            if (delegate != null) {
                delegate.error(marker, format, arg1, arg2);
                return;
            }
            error(format, arg1, arg2);
        }

        @Override
        public void error(Marker marker, String format, Object... arguments) {
            if (delegate != null) {
                delegate.error(marker, format, arguments);
                return;

            }
            error(format, arguments);
        }

        @Override
        public void error(Marker marker, String msg, Throwable t) {
            if (delegate != null) {
                delegate.error(marker, msg, t);
                return;
            }
            error(msg, t);
        }

        private void log(LogLevel level, @Nullable String message, @Nullable Throwable throwable) {
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

                String formatted = String.format("%s [%s] %s  [%s] %s", dateFormat.format(new Date()),
                        Thread.currentThread().getName(), level.name(), name, combinedMessage);
                System.out.println(formatted);
            }
        }
    }
}
