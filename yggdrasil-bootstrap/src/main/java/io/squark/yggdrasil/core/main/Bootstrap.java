package io.squark.yggdrasil.core.main;

import io.squark.yggdrasil.core.api.Constants;
import io.squark.yggdrasil.core.api.util.LibHelper;
import io.squark.yggdrasil.core.api.util.ReflectionUtil;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import io.squark.yggdrasil.nestedjarclassloader.BootstrapClassLoader;
import io.squark.yggdrasil.nestedjarclassloader.NestedJarClassLoader;
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
            logger.info("Bootstrapping Yggdrasil");

            BootstrapClassLoader isolated = new BootstrapClassLoader(LibHelper.getOwnJar());

            Class<?> coreClassLoaderClass = isolated.loadClass(NestedJarClassLoader.class.getName());
            Constructor constructor = coreClassLoaderClass.getDeclaredConstructor(ClassLoader.class);
            ClassLoader coreClassLoader = (ClassLoader) constructor.newInstance(isolated);

            Method addURLs = coreClassLoaderClass.getDeclaredMethod("addURLs", URL[].class);
            addURLs.invoke(coreClassLoader, (Object) LibHelper.getLibs(Bootstrap.class, Constants.YGGDRASIL_RUNTIME_LIB_PATH));

            Thread.currentThread().setContextClassLoader(coreClassLoader);
            ReflectionUtil.invokeMethod("internalMain", Constants.YGGDRASIL_CLASS_NAME, null, new Object[]{args}, null,
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
