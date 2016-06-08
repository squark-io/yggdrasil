package io.hakansson.dynamicjar.logging.api;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-07.
 * Copyright 2016
 */
public interface ReplaceableLoggerBinder {
    void register(CrappyLoggerFactory.CrappyLogger logger);
    void notifyLoggingInitialized();
}
