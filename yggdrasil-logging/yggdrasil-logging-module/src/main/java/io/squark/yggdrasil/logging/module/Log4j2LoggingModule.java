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
package io.squark.yggdrasil.logging.module;

import io.squark.yggdrasil.core.api.Constants;
import io.squark.yggdrasil.core.api.exception.DependencyResolutionException;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.logging.LoggingModule;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.ContextAnchor;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.ILoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static org.apache.logging.log4j.core.impl.ContextAnchor.THREAD_CONTEXT;

public class Log4j2LoggingModule implements LoggingModule {

    private static final List<String> validConfigFiles = new ArrayList<>();

    static {
        validConfigFiles.add("log4j2.properties");
        validConfigFiles.add("log4j2.yaml");
        validConfigFiles.add("log4j2.yml");
        validConfigFiles.add("log4j2.json");
        validConfigFiles.add("log4j2.jsn");
        validConfigFiles.add("log4j2.xml");
    }

    @Override
    public ILoggerFactory initialize(@Nullable YggdrasilConfiguration configuration, @Nullable ClassLoader classLoader,
        @Nullable URL jarWithConfig) throws YggdrasilException {

        String systemLogLevel = System.getProperty(Constants.YGGDRASIL_LOG_LEVEL);
        String systemConfigFile = System.getProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);

        if (systemLogLevel != null) {
            System.setProperty(LogManager.FACTORY_PROPERTY_NAME, CustomLoggerContextFactory.class.getName());
            Level log4jLevel = Level.toLevel(systemLogLevel);
            CustomLoggerContextFactory.customLoggerContext = getCustomLoggerContext(log4jLevel);
            return new Log4jLoggerFactory();
        } else if (systemConfigFile == null && jarWithConfig != null) {
            try {
                InputStream inputStream = jarWithConfig.openStream();
                JarInputStream jarInputStream = new JarInputStream(inputStream);
                JarEntry jarEntry;

                while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                    if (jarEntry.isDirectory()) continue;
                    String fileName = FilenameUtils.getName(jarEntry.getName());
                    if (validConfigFiles.contains(fileName)) {
                        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, jarWithConfig.getFile() + "!/" + jarEntry.getName());
                        return new Log4jLoggerFactory();
                    }
                }
            } catch (IOException e) {
                throw new DependencyResolutionException(e);
            }
        }
        //Fallback:
        System.setProperty(LogManager.FACTORY_PROPERTY_NAME, CustomLoggerContextFactory.class.getName());
        CustomLoggerContextFactory.customLoggerContext = getCustomLoggerContext(Level.INFO);

        return new Log4jLoggerFactory();
    }

    private LoggerContext getCustomLoggerContext(Level log4jLevel) throws YggdrasilException {
        LoggerContext ctx = ContextAnchor.THREAD_CONTEXT.get();
        if (ctx == null) {
            ctx = new CustomLoggerContext();
        }
        if (ctx.getState() == LifeCycle.State.INITIALIZED) {
            ctx.start(new CustomLoggerConfiguration(log4jLevel));
        }

        return ctx;
    }

    //Needs to be public since it is will be instantiated (by reflection) from Log4j classes.
    @SuppressWarnings("WeakerAccess")
    public static class CustomLoggerContextFactory extends Log4jContextFactory {

        static LoggerContext customLoggerContext;

        @Override
        public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext,
            boolean currentContext) {
            final LoggerContext ctx = customLoggerContext;
            if(externalContext != null && ctx.getExternalContext() == null) {
                ctx.setExternalContext(externalContext);
            }
            if(ctx.getState() == LifeCycle.State.INITIALIZED) {
                ctx.start();
            }
            return ctx;
        }

        @Override
        public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext, boolean currentContext,
            URI configLocation, String name) {
            final LoggerContext ctx = customLoggerContext;
            if (externalContext != null && ctx.getExternalContext() == null) {
                ctx.setExternalContext(externalContext);
            }
            if (name != null) {
                ctx.setName(name);
            }
            if (ctx.getState() == LifeCycle.State.INITIALIZED) {
                if (configLocation != null || name != null) {
                    THREAD_CONTEXT.set(ctx);
                    final Configuration config = ConfigurationFactory.getInstance().getConfiguration(name, configLocation);
                    StatusLogger.getLogger().debug("Starting LoggerContext[name={}] from configuration at {}", ctx.getName(), configLocation);
                    ctx.start(config);
                    THREAD_CONTEXT.remove();
                } else {
                    ctx.start();
                }
            }
            return ctx;
        }
    }

    private static class CustomLoggerConfiguration extends DefaultConfiguration {

        private Level level;

        CustomLoggerConfiguration(Level level) {
            this.level = level;
        }

        @Override
        protected void setToDefault() {
            setName(DefaultConfiguration.DEFAULT_NAME + "@" + Integer.toHexString(hashCode()));
            final Layout<? extends Serializable> layout = PatternLayout.newBuilder()
                .withPattern(DefaultConfiguration.DEFAULT_PATTERN)
                .withConfiguration(this)
                .build();
            final Appender appender = ConsoleAppender.createDefaultAppenderForLayout(layout);
            appender.start();
            addAppender(appender);
            final LoggerConfig rootLoggerConfig = getRootLogger();
            rootLoggerConfig.addAppender(appender, null, null);

            rootLoggerConfig.setLevel(level != null ? level : Level.INFO);
        }
    }

    private static class CustomLoggerContext extends LoggerContext {

        CustomLoggerContext() {
            super("Default", null, (URI) null);
        }

        @Override
        public void reconfigure() {
            //do nothing
        }
    }
}
