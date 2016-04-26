package io.hakansson.dynamicjar.core.api;

import java.util.HashMap;
import java.util.Map;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-04-02.
 * Copyright 2016
 */
public class DynamicJarContext {
    private static Map<String, Object> registeredObjects = new HashMap<>();

    public static void registerObject(String name, Object object) {
        registeredObjects.put(object.getClass().getName(), object);
    }

    public static Object getObject(String name) {
        return registeredObjects.get(name);
    }
}
