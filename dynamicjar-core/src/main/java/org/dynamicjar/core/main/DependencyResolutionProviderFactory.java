package org.dynamicjar.core.main;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.dynamicjar.core.api.DependencyResolutionProvider;
import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.api.util.LambdaExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-21.
 * Copyright 2016
 */
public class DependencyResolutionProviderFactory {

    private static Logger logger = LoggerFactory.getLogger(DynamicJar.class);

    static DependencyResolutionProvider getFirstDependencyResolver() throws DependencyResolutionException {
        Optional<Class<? extends DependencyResolutionProvider>> dependencyResolverOptional = getDependencyResolvers().stream().findFirst();
        if (dependencyResolverOptional.isPresent()) {
            try {
                return dependencyResolverOptional.get().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new DependencyResolutionException(e);
            }
        }
        throw new DependencyResolutionException("Failed to find DependencyResolver");
    }

    static Collection<Class<? extends DependencyResolutionProvider>> getDependencyResolvers()
        throws DependencyResolutionException {
        Long before = System.currentTimeMillis();
        Map<String, Class<? extends DependencyResolutionProvider>> matches = new ConcurrentHashMap<>();
        new FastClasspathScanner("").scan().getNamesOfClassesImplementing(DependencyResolutionProvider.class)
            .parallelStream().forEach(LambdaExceptionUtil.rethrowConsumer(className -> {
            try {
                //noinspection unchecked
                matches
                    .put(className, (Class<? extends DependencyResolutionProvider>) Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new DependencyResolutionException(e);
            }
        }));
        logger.debug(
            "Scanning classpath for implementations of [" + DependencyResolutionProvider.class.getName() +
            "] took " + (System.currentTimeMillis() - before) + "ms.");
        return matches.values();
    }
}