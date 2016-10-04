package io.squark.yggdrasil.core.api.logging;

import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import org.jetbrains.annotations.Nullable;
import org.slf4j.ILoggerFactory;

import java.net.URL;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-05-08.
 * Copyright 2016
 */
public interface LoggingModule {
    ILoggerFactory initialize(@Nullable YggdrasilConfiguration configuration, @Nullable ClassLoader classLoader, @Nullable URL jarWithConfig)
    throws YggdrasilException;
}
