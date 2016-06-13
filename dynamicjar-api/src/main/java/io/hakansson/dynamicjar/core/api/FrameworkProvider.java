package io.hakansson.dynamicjar.core.api;

import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-09.
 * Copyright 2016
 */
public interface FrameworkProvider {
    void provide(@Nullable DynamicJarConfiguration configuration) throws DynamicJarException;

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default List<ProviderDependency> runBefore() {
        return Collections.emptyList();
    }

    default List<ProviderDependency> runAfter() {
        return Collections.emptyList();
    }

    class ProviderDependency {
        public String name;
        public boolean optional;

        public ProviderDependency(String name, boolean optional) {
            this.name = name;
            this.optional = optional;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ProviderDependency that = (ProviderDependency) o;

            return name.equals(that.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
