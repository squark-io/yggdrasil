package io.squark.ask.core.api.util;

import io.squark.ask.core.api.exception.ProviderException;
import io.squark.ask.core.api.model.AskConfiguration;

import java.io.*;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-04-16.
 * Copyright 2016
 */
public class ConfigurationSerializer {
    public static byte[] serializeConfig(AskConfiguration configuration) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(configuration);
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static AskConfiguration deserializeConfig(byte[] buffer) throws ProviderException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (AskConfiguration) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ProviderException(e);
        }
    }
}
