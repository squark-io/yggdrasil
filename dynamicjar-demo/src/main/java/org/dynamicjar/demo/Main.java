package org.dynamicjar.demo;

import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.main.DynamicJar;

/**
 * *** DynamicJar ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-05.
 * Copyright 2016
 */
public class Main {
    public static void main(String[] args) {
        try {
            DynamicJar.loadDependencies(Main.class, "org.dynamicjar", "dynamicjar-demo");
            new Business().helloWorld();
        } catch (DependencyResolutionException e) {
            e.printStackTrace();
        }
    }
}
