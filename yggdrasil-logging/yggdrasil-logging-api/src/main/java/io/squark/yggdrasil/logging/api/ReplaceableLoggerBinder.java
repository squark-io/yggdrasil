package io.squark.yggdrasil.logging.api;

import org.slf4j.ILoggerFactory;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-06-07.
 * Copyright 2016
 */
public interface ReplaceableLoggerBinder {
    void register(CrappyLogger logger);
    void notifyLoggingInitialized(ILoggerFactory loggerFactory);
}
