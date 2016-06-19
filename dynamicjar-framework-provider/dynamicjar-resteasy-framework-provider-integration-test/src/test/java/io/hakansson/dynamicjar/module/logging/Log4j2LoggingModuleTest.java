package io.hakansson.dynamicjar.module.logging;

import io.hakansson.dynamicjar.core.api.Constants;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-18.
 * Copyright 2016
 */
public class Log4j2LoggingModuleTest {
    @Test
    public void initialize() throws Exception {
        Log4j2LoggingModule loggingModule = new Log4j2LoggingModule();
        System.setProperty(Constants.DYNAMICJAR_LOG_LEVEL, "TRACE");
        loggingModule.initialize(null, null, null);
        System.clearProperty(Constants.DYNAMICJAR_LOG_LEVEL);
        System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log4j2.properties");
        loggingModule.initialize(null, null, null);
        System.clearProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        URLConnection connection = Mockito.mock(URLConnection.class);
        URL jarWithConfig = new URL("", "", 99, "/", new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return connection;
            }
        });
        InputStream inputStream = Mockito.mock(InputStream.class);
        Mockito.when(inputStream.read(Mockito.any(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(5);
        Mockito.when(jarWithConfig.openStream()).thenReturn(inputStream);
        loggingModule.initialize(null, null, jarWithConfig);

    }


}