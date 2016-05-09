package io.hakansson.dynamicjar.core.main;

import io.hakansson.dynamicjar.core.api.Constants;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.exception.NestedJarClassloaderException;
import io.hakansson.dynamicjar.core.main.util.LibHelper;
import io.hakansson.dynamicjar.logging.api.InternalLogger;
import io.hakansson.dynamicjar.logging.api.LogLevel;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Erik Håkansson on 2016-05-09.
 * WirelessCar
 */
public class Bootstrap {

    private static InternalLogger logger = InternalLogger.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        if (!(Bootstrap.class.getClassLoader() instanceof NestedJarClassLoader)) {
            //Rerun main in correct classloader
            try {
                NestedJarClassLoader helperClassLoader =
                    new NestedJarClassLoader(LibHelper.getLibs(Constants.DYNAMICJAR_RUNTIME_LIB_PATH),
                        Bootstrap.class.getClassLoader());
                Thread.currentThread().setContextClassLoader(helperClassLoader);
                try {
                    Method internalMainMethod = Class.forName("io.hakansson.dynamicjar.core.main.DynamicJar", true, helperClassLoader)
                        .getDeclaredMethod("internalMain", String[].class);
                    internalMainMethod.setAccessible(true);
                    internalMainMethod.invoke(null, new Object[] { args });
                } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
                    throw new NestedJarClassloaderException(e);
                }

            } catch (DynamicJarException e) {
                logger.log(LogLevel.ERROR, e);
            }
        } else {
            DynamicJar.internalMain(args);
        }
    }
}
