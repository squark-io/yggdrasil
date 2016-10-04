package io.squark.dynamicjar.core.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-26.
 * Copyright 2016
 */
public class ReflectionUtil {

    private static Map<String, Method> cachedMethods = new HashMap<>();

    public static <T> T invokeMethod(@NotNull String methodName, String className, @Nullable Object instance,
            @Nullable Object[] args, @Nullable Class[] argsTypeOverrides, @Nullable ClassLoader classLoader,
            @Nullable Class<? extends T> returnType) throws Throwable
    {
        if (args != null && argsTypeOverrides != null && args.length != argsTypeOverrides.length) {
            throw new IllegalStateException("Type override must be same size as arguments array");
        }

        String signature = className + "!" + methodName;

        Class<?>[] argsTypes = argsTypeOverrides;
        Object[] argsObjects = null;
        if (args != null) {
            if (argsTypeOverrides == null) {
                argsTypes = new Class[args.length];
            }
            argsObjects = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                argsObjects[i] = args[i] == null ? null : args[i];
                if (argsTypeOverrides == null) {
                    argsTypes[i] = args[i] == null ? null : args[i].getClass();
                }
                signature += '#' + argsTypes[i].getName();
            }
        }

        if (classLoader == null) {
            if (instance != null) {
                classLoader = instance.getClass().getClassLoader();
            } else {
                classLoader = ReflectionUtil.class.getClassLoader();
            }
        }
        Class<?> type = Class.forName(className, true, classLoader);
        Method method = cachedMethods.get(signature);
        if (method == null) {
            try {
                method = type.getMethod(methodName, argsTypes);
            } catch (NoSuchMethodException e) {
                method = type.getDeclaredMethod(methodName, argsTypes);
            }
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            cachedMethods.put(signature, method);
        }
        try {
            Object invocationResult = method.invoke(instance, argsObjects);
            if (returnType != null) {
                return returnType.cast(invocationResult);
            } else {
                return null;
            }
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = e.getCause();
            }
            throw e;
        }
    }
}
