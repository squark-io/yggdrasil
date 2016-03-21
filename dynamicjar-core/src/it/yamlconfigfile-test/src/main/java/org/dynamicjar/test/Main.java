package org.dynamicjar.test;

import org.dynamicjar.core.api.exception.DynamicJarException;
import org.dynamicjar.core.main.DynamicJar;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-19.
 * Copyright 2016
 */
public class Main {

    public static void main(String[] args) {
        try {
            DynamicJar.initiate(Main.class);
            new YamlConfigFileTestTarget().helloWorld();
        } catch (DynamicJarException e) {
            e.printStackTrace();
        }
    }

}
