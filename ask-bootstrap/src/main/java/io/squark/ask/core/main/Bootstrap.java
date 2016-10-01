package io.squark.ask.core.main;

import io.squark.ask.core.api.Constants;
import io.squark.ask.core.api.util.LibHelper;
import io.squark.ask.core.api.util.ReflectionUtil;
import io.squark.ask.logging.api.InternalLoggerBinder;
import io.squark.ask.nestedjarclassloader.BootstrapClassLoader;
import io.squark.ask.nestedjarclassloader.NestedJarClassLoader;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * Created by Erik Håkansson on 2016-05-09.
 * WirelessCar
 */
public class Bootstrap {

    private static Logger logger = InternalLoggerBinder.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        //Run main in correct classloader
        try {
            logger.info("Bootstrapping Ask");

            BootstrapClassLoader isolated = new BootstrapClassLoader(LibHelper.getOwnJar());

            Class<?> coreClassLoaderClass = isolated.loadClass(NestedJarClassLoader.class.getName());
            Constructor constructor = coreClassLoaderClass.getDeclaredConstructor(ClassLoader.class);
            ClassLoader coreClassLoader = (ClassLoader) constructor.newInstance(isolated);

            Method addURLs = coreClassLoaderClass.getDeclaredMethod("addURLs", URL[].class);
            addURLs.invoke(coreClassLoader, (Object) LibHelper.getLibs(Bootstrap.class, Constants.ASK_RUNTIME_LIB_PATH));

            Thread.currentThread().setContextClassLoader(coreClassLoader);
            ReflectionUtil.invokeMethod("internalMain", Constants.ASK_CLASS_NAME, null, new Object[]{args}, null,
                    coreClassLoader, null);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = e.getCause();
            }
            logger.error(null, e);
            System.exit(1);
        }

    }
}