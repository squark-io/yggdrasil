package io.hakansson.dynamicjar.core.main;

import io.hakansson.dynamicjar.core.api.Constants;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.exception.NestedJarClassloaderException;
import io.hakansson.dynamicjar.core.api.util.LibHelper;
import io.hakansson.dynamicjar.logging.api.InternalLogger;
import io.hakansson.dynamicjar.logging.api.LogLevel;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by Erik Håkansson on 2016-05-09.
 * WirelessCar
 */
public class Bootstrap {

    private static InternalLogger logger = InternalLogger.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        //Run main in correct classloader
        try {
            logger.log(LogLevel.INFO, "Bootstrapping DynamicJar");
            NestedJarClassLoader coreClassLoader =
                new NestedJarClassLoader(LibHelper.getLibs(Constants.DYNAMICJAR_RUNTIME_LIB_PATH), null, true);
            LibHelper.copyResourcesIntoClassLoader(coreClassLoader, "META-INF/",
                Arrays.asList(Constants.LIB_PATH, Constants.DYNAMICJAR_RUNTIME_LIB_PATH));
            Thread.currentThread().setContextClassLoader(coreClassLoader);
            try {
                Method internalMainMethod = Class.forName(Constants.DYNAMIC_JAR_CLASS_NAME, true, coreClassLoader)
                    .getDeclaredMethod("internalMain", String[].class);
                internalMainMethod.setAccessible(true);
                internalMainMethod.invoke(null, new Object[]{args});
            } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException |
                IllegalAccessException e) {
                throw new NestedJarClassloaderException(e);
            }

        } catch (DynamicJarException e) {
            logger.log(LogLevel.ERROR, e);
        }

    }
}
