package io.squark.yggdrasil.logging.api;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-06-07.
 * Copyright 2016
 */
public final class CrappyLoggerFactory implements ILoggerFactory {

    private ReplaceableLoggerBinder replaceableLoggerBinder;

    CrappyLoggerFactory(ReplaceableLoggerBinder staticLoggerBinder) {
        replaceableLoggerBinder = staticLoggerBinder;
    }

    @Override
    public Logger getLogger(String name) {
        CrappyLogger logger = new CrappyLogger(name);
        replaceableLoggerBinder.register(logger);
        return logger;
    }

}
