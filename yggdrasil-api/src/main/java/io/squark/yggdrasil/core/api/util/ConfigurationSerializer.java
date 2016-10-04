package io.squark.yggdrasil.core.api.util;

import io.squark.yggdrasil.core.api.exception.ProviderException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;

import java.io.*;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-04-16.
 * Copyright 2016
 */
public class ConfigurationSerializer {
    public static byte[] serializeConfig(YggdrasilConfiguration configuration) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(configuration);
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static YggdrasilConfiguration deserializeConfig(byte[] buffer) throws ProviderException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (YggdrasilConfiguration) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new ProviderException(e);
        }
    }
}
