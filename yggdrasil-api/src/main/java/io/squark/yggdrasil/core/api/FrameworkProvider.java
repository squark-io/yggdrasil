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
package io.squark.yggdrasil.core.api;

import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface FrameworkProvider {
    void provide(@Nullable YggdrasilConfiguration configuration) throws YggdrasilException;

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
