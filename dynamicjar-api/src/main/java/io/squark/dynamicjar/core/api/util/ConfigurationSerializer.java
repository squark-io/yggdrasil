package io.squark.dynamicjar.core.api.util;

import io.squark.dynamicjar.core.api.exception.ProviderException;
import io.squark.dynamicjar.core.api.model.DynamicJarConfiguration;

import java.io.*;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-04-16.
 * Copyright 2016
 */
public class ConfigurationSerializer {
    public static byte[] serializeConfig(DynamicJarConfiguration configuration) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(configuration);
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static DynamicJarConfiguration deserializeConfig(byte[] buffer) throws ProviderException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (DynamicJarConfiguration) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ProviderException(e);
        }
    }
}
