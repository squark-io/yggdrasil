package io.squark.yggdrasil.logging.api;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-06-10.
 * Copyright 2016
 */
public class CrappyLogger implements Logger {

    private static final String YGGDRASIL_LOG_LEVEL = "yggdrasil.logLevel";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    private boolean replaced;
    private String name;
    private LogLevel logLevel;
    private Logger delegate;

    CrappyLogger(String name) {
        this.name = name;
        logLevel = LogLevel.INFO;
        if (System.getProperty(YGGDRASIL_LOG_LEVEL) != null) {
            logLevel = LogLevel.valueOf(System.getProperty(YGGDRASIL_LOG_LEVEL));
        }
    }

    boolean isReplaced() {
        return replaced;
    }

    void setDelegate(Logger delegate) {
        this.delegate = delegate;
        if (this.delegate != null) {
            replaced = true;
        }
    }

    @Override
    public String getName() {
        if (replaced) {
            return delegate.getName();
        }
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        if (replaced) {
            return delegate.isTraceEnabled();
        }
        return (logLevel.intLevel() >= LogLevel.TRACE.intLevel());
    }

    @Override
    public void trace(String msg) {
        if (replaced) {
            delegate.trace(msg);
            return;
        }
        log(LogLevel.TRACE, msg, null);
    }

    @Override
    public void trace(String format, Object arg) {
        if (replaced) {
            delegate.trace(format, arg);
            return;
        }
        log(LogLevel.TRACE, String.format(format, arg), null);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (replaced) {
            delegate.trace(format, arg1, arg2);
            return;
        }
        log(LogLevel.TRACE, String.format(format, arg1, arg2), null);
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (replaced) {
            delegate.trace(format, arguments);
            return;
        }
        log(LogLevel.TRACE, String.format(format, arguments), null);
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (replaced) {
            delegate.trace(msg, t);
            return;
        }
        log(LogLevel.TRACE, null, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        if (replaced) {
            return delegate.isTraceEnabled(marker);
        }
        return isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String msg) {
        if (replaced) {
            delegate.trace(marker, msg);
            return;
        }
        trace(msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (replaced) {
            delegate.trace(marker, format, arg);
            return;
        }
        trace(format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (replaced) {
            delegate.trace(marker, format, arg1, arg2);
            return;
        }
        trace(format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        if (replaced) {
            delegate.trace(marker, format, argArray);
            return;
        }
        trace(format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (replaced) {
            delegate.trace(marker, msg, t);
            return;
        }
        trace(msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        if (replaced) {
            return delegate.isDebugEnabled();
        }
        return (logLevel.intLevel() >= LogLevel.DEBUG.intLevel());
    }

    @Override
    public void debug(String msg) {
        if (replaced) {
            delegate.debug(msg);
            return;
        }
        log(LogLevel.DEBUG, msg, null);
    }

    @Override
    public void debug(String format, Object arg) {
        if (replaced) {
            delegate.debug(format, arg);
            return;
        }
        log(LogLevel.DEBUG, String.format(format, arg), null);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (replaced) {
            delegate.debug(format, arg1, arg2);
            return;
        }
        log(LogLevel.DEBUG, String.format(format, arg1, arg2), null);
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (replaced) {
            delegate.debug(format, arguments);
            return;
        }
        log(LogLevel.DEBUG, String.format(format, arguments), null);
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (replaced) {
            delegate.debug(msg, t);
            return;
        }
        log(LogLevel.DEBUG, msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        if (replaced) {
            return delegate.isDebugEnabled(marker);
        }
        return isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String msg) {
        if (replaced) {
            delegate.debug(marker, msg);
            return;
        }
        debug(msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (replaced) {
            delegate.debug(marker, format, arg);
            return;
        }
        debug(format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (replaced) {
            delegate.debug(marker, format, arg1, arg2);
            return;
        }
        debug(format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        if (replaced) {
            delegate.debug(marker, format, arguments);
            return;
        }
        debug(format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        if (replaced) {
            delegate.debug(marker, msg, t);
            return;
        }
        debug(msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        if (replaced) {
            return delegate.isInfoEnabled();
        }
        return (logLevel.intLevel() >= LogLevel.INFO.intLevel());
    }

    @Override
    public void info(String msg) {
        if (replaced) {
            delegate.info(msg);
            return;
        }
        log(LogLevel.INFO, msg, null);
    }

    @Override
    public void info(String format, Object arg) {
        if (replaced) {
            delegate.info(format, arg);
            return;
        }
        log(LogLevel.INFO, String.format(format, arg), null);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (replaced) {
            delegate.info(format, arg1, arg2);
            return;
        }
        log(LogLevel.INFO, String.format(format, arg1, arg2), null);
    }

    @Override
    public void info(String format, Object... arguments) {
        if (replaced) {
            delegate.info(format, arguments);
            return;
        }
        log(LogLevel.INFO, String.format(format, arguments), null);
    }

    @Override
    public void info(String msg, Throwable t) {
        if (replaced) {
            delegate.info(msg, t);
            return;
        }
        log(LogLevel.INFO, msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        if (replaced) {
            return delegate.isInfoEnabled(marker);
        }
        return isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String msg) {
        if (replaced) {
            delegate.info(marker, msg);
            return;
        }
        info(msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        if (replaced) {
            delegate.info(marker, format, arg);
            return;
        }
        info(format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (replaced) {
            delegate.info(marker, format, arg1, arg2);
            return;
        }
        info(format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        if (replaced) {
            delegate.info(marker, format, arguments);
            return;
        }
        info(format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (replaced) {
            delegate.info(marker, msg, t);
            return;
        }
        info(msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        if (replaced) {
            return delegate.isWarnEnabled();
        }
        return (logLevel.intLevel() >= LogLevel.WARN.intLevel());
    }

    @Override
    public void warn(String msg) {
        if (replaced) {
            delegate.warn(msg);
            return;
        }
        log(LogLevel.WARN, msg, null);
    }

    @Override
    public void warn(String format, Object arg) {
        if (replaced) {
            delegate.warn(format, arg);
            return;
        }
        log(LogLevel.WARN, String.format(format, arg), null);
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (replaced) {
            delegate.warn(format, arguments);
            return;
        }
        log(LogLevel.WARN, String.format(format, arguments), null);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (replaced) {
            delegate.warn(format, arg1, arg2);
            return;
        }
        log(LogLevel.WARN, String.format(format, arg1, arg2), null);
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (replaced) {
            delegate.warn(msg, t);
            return;
        }
        log(LogLevel.WARN, msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        if (replaced) {
            return delegate.isWarnEnabled(marker);
        }
        return isWarnEnabled();
    }

    @Override
    public void warn(Marker marker, String msg) {
        if (replaced) {
            delegate.warn(marker, msg);
            return;
        }
        warn(msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (replaced) {
            delegate.warn(marker, format, arg);
            return;
        }
        warn(format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (replaced) {
            delegate.warn(marker, format, arg1, arg2);
            return;
        }
        warn(format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        if (replaced) {
            delegate.warn(marker, format, arguments);
            return;
        }
        warn(format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (replaced) {
            delegate.warn(marker, msg, t);
            return;
        }
        warn(msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        if (replaced) {
            return delegate.isErrorEnabled();
        }
        return (logLevel.intLevel() >= LogLevel.ERROR.intLevel());
    }

    @Override
    public void error(String msg) {
        if (replaced) {
            delegate.error(msg);
            return;
        }
        log(LogLevel.ERROR, msg, null);
    }

    @Override
    public void error(String format, Object arg) {
        if (replaced) {
            delegate.error(format, arg);
            return;
        }
        log(LogLevel.ERROR, String.format(format, arg), null);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (replaced) {
            delegate.error(format, arg1, arg2);
            return;
        }
        log(LogLevel.ERROR, String.format(format, arg1, arg2), null);
    }

    @Override
    public void error(String format, Object... arguments) {
        if (replaced) {
            delegate.error(format, arguments);
            return;
        }
        log(LogLevel.ERROR, String.format(format, arguments), null);
    }

    @Override
    public void error(String msg, Throwable t) {
        if (replaced) {
            delegate.error(msg, t);
            return;
        }
        log(LogLevel.ERROR, msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        if (replaced) {
            return delegate.isErrorEnabled(marker);
        }
        return isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String msg) {
        if (replaced) {
            delegate.error(marker, msg);
            return;
        }
        error(msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if (replaced) {
            delegate.error(marker, format, arg);
            return;
        }
        error(format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (replaced) {
            delegate.error(marker, format, arg1, arg2);
            return;
        }
        error(format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        if (replaced) {
            delegate.error(marker, format, arguments);
            return;

        }
        error(format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (replaced) {
            delegate.error(marker, msg, t);
            return;
        }
        error(msg, t);
    }

    private void log(LogLevel level, @Nullable String message, @Nullable Throwable throwable) {
        if (logLevel == null) {
            logLevel = LogLevel.INFO;
            if (System.getProperty(YGGDRASIL_LOG_LEVEL) != null) {
                logLevel = LogLevel.valueOf(System.getProperty(YGGDRASIL_LOG_LEVEL));
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
            String formatted = String.format("%s [%s] %-5s %s - %s", DATE_FORMAT.format(new Date()),
                    Thread.currentThread().getName(), level.name(), name, combinedMessage);
            System.out.println(formatted);
        }
    }
}
