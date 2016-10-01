package io.squark.ask.core.api.logging;

import io.squark.ask.core.api.exception.AskException;
import io.squark.ask.core.api.model.AskConfiguration;
import org.jetbrains.annotations.Nullable;
import org.slf4j.ILoggerFactory;

import java.net.URL;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-05-08.
 * Copyright 2016
 */
public interface LoggingModule {
    ILoggerFactory initialize(@Nullable AskConfiguration configuration, @Nullable ClassLoader classLoader, @Nullable URL jarWithConfig)
    throws AskException;
}
