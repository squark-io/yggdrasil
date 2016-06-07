package org.slf4j.impl;

import io.hakansson.dynamicjar.logging.api.CrappyLoggerFactory;
import io.hakansson.dynamicjar.logging.api.ReplaceableLoggerBinder;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.spi.LoggerFactoryBinder;

import java.util.HashSet;
import java.util.Set;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-07.
 * Copyright 2016
 */
public class StaticLoggerBinder implements LoggerFactoryBinder, ReplaceableLoggerBinder {

    public static String REQUESTED_API_VERSION = "1.7.21"; // !final
    private final CrappyLoggerFactory loggerFactory;

    private boolean isValidated;

    private static StaticLoggerBinder INSTANCE = new StaticLoggerBinder();

    private static String loggerFactoryClassStr = CrappyLoggerFactory.class.getName();

    private Set<Logger> loggers = new HashSet<>();

    private StaticLoggerBinder() {
        loggerFactory = new CrappyLoggerFactory(this);
    }

    public static StaticLoggerBinder getSingleton() {
        return INSTANCE;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return loggerFactoryClassStr;
    }

    @Override
    public void register(Logger logger) {
        loggers.add(logger);
    }
}
