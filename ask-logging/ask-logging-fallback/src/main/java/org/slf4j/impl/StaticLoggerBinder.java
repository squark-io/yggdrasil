package org.slf4j.impl;

import io.squark.ask.logging.api.CrappyLoggerFactory;
import io.squark.ask.logging.api.InternalLoggerBinder;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-06-07.
 * Copyright 2016
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

    private static StaticLoggerBinder INSTANCE = new StaticLoggerBinder();

    private static String loggerFactoryClassStr = CrappyLoggerFactory.class.getName();


    private StaticLoggerBinder() {
    }

    public static StaticLoggerBinder getSingleton() {
        return INSTANCE;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return InternalLoggerBinder.getSingleton().getLoggerFactory();
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return loggerFactoryClassStr;
    }

}
