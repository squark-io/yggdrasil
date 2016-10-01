package io.squark.ask.core.api;

import io.squark.ask.core.api.exception.DependencyResolutionException;
import io.squark.ask.core.api.model.AskDependency;

import java.util.Set;

/**
 * *** Ask ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-12.
 * Copyright 2016
 */
public interface DependencyResolutionProvider {

    Set<AskDependency> resolveDependencies(Set<AskDependency> dependencies,
        boolean loadTransitiveProvidedDependencies) throws DependencyResolutionException;

}
