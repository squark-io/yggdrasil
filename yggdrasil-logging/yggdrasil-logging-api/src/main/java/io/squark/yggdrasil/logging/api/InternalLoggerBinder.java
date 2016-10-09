/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.squark.yggdrasil.logging.api;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

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
