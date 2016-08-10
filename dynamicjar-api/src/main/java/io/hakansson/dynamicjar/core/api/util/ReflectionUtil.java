package io.hakansson.dynamicjar.core.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-26.
 * Copyright 2016
 */
public class ReflectionUtil {
    public static <T> Object invokeMethod(@NotNull String methodName, Class<T> type, @Nullable T instance,
                                          @Nullable Object[] args, @Nullable Class[] argsTypeOverrides) throws
            ClassNotFoundException, NoSuchMethodException, IllegalAccessException
    {
        if (args != null && argsTypeOverrides != null && args.length != argsTypeOverrides.length) {
            throw new IllegalStateException("Type override must be same size as arguments array");
        }
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
            }
        }
        Method method;
        try {
            method = type.getMethod(methodName, argsTypes);
        } catch (NoSuchMethodException e) {
            method = type.getDeclaredMethod(methodName, argsTypes);
        }
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        try {
            return method.invoke(instance, argsObjects);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = e.getCause();
            }
            throw new RuntimeException(e);
        }
    }
}
