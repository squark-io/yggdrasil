package io.hakansson.dynamicjar.core.api.model;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-04-27.
 * Copyright 2016
 */
public abstract class ProviderConfiguration {
    public abstract String getIdentifier();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
