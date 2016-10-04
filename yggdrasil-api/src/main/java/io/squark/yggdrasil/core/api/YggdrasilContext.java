package io.squark.yggdrasil.core.api;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-04-02.
 * Copyright 2016
 */
public class YggdrasilContext {
    private static Map<String, Object> registeredObjects = new HashMap<>();
    private static URL overriddenLibraryPath;

    public static void registerObject(String name, Object object) {
        registeredObjects.put(name, object);
    }

    public static Object getObject(String name) {
        return registeredObjects.get(name);
    }

    public static <T> T getObject(Class<T> type) {
        return type.cast(registeredObjects.get(type.getName()));
    }

    public static void overrideLibraryPath(URL resource) {
        overriddenLibraryPath = resource;
    }

    public static URL getOverriddenLibraryPath() {
        return overriddenLibraryPath;
    }
}
