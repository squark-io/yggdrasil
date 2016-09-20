package io.hakansson.dynamicjar.logging.api;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-07.
 * Copyright 2016
 */
public class InternalLoggerBinder implements ReplaceableLoggerBinder {

    private CrappyLoggerFactory crappyLoggerFactory;
    private ILoggerFactory delegateLoggerFactory;

    private static InternalLoggerBinder INSTANCE = new InternalLoggerBinder();

    private final Set<CrappyLogger> loggers = new HashSet<>();
    private boolean replaced;

    private InternalLoggerBinder() {
        crappyLoggerFactory = new CrappyLoggerFactory(this);
    }

    public static InternalLoggerBinder getSingleton() {
        return INSTANCE;
    }

    public static Logger getLogger(Class aClass) {
        return getLogger(aClass.getName());
    }

    public static Logger getLogger(String name) {
        return getSingleton().getLoggerFactory().getLogger(name);
    }

    public ILoggerFactory getLoggerFactory() {
        return replaced ? delegateLoggerFactory : crappyLoggerFactory;
    }

    @Override
    public void register(CrappyLogger logger) {
        synchronized (loggers) {
            loggers.add(logger);
        }
    }

    @Override
    public void notifyLoggingInitialized(ILoggerFactory loggerFactory) {
        synchronized (loggers) {
            this.delegateLoggerFactory = loggerFactory;
            this.crappyLoggerFactory = null;
            Set<CrappyLogger> loggersToRemove = new HashSet<>();
            for (CrappyLogger logger : loggers) {
                if (!logger.isReplaced()) {
                    logger.setDelegate(loggerFactory.getLogger(logger.getName()));
                }
                loggersToRemove.add(logger);
            }
            loggers.removeAll(loggersToRemove);
            replaced = true;
        }
    }
}
