package io.hakansson.dynamicjar.core.it;

import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.main.DynamicJar;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-19.
 * Copyright 2016
 */
public class JsonMain {

    public static void main(String[] args) {
        try {
            DynamicJar.initiate(JsonMain.class);
            new JsonConfigFileTestTarget().helloWorld();
        } catch (DynamicJarException e) {
            e.printStackTrace();
        }
    }

}
