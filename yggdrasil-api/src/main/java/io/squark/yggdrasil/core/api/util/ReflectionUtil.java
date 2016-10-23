/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.squark.yggdrasil.core.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
                signature += '#' + (argsTypes[i] != null ? argsTypes[i].getName() : "null");
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
