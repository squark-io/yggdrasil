package io.squark.yggdrasil.core.api;

import io.squark.yggdrasil.core.api.exception.DependencyResolutionException;
import io.squark.yggdrasil.core.api.model.YggdrasilDependency;

import java.util.Set;

/**
 * *** Yggdrasil ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-12.
 * Copyright 2016
 */
public interface DependencyResolutionProvider {

    Set<YggdrasilDependency> resolveDependencies(Set<YggdrasilDependency> dependencies,
        boolean loadTransitiveProvidedDependencies) throws DependencyResolutionException;

}
