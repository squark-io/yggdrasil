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
package io.squark.yggdrasil.core.main;

import io.squark.nestedjarclassloader.NestedJarClassLoader;
import io.squark.yggdrasil.core.api.DependencyResolutionProvider;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.core.api.model.YggdrasilDependency;
import io.squark.yggdrasil.core.api.util.Scopes;
import io.squark.yggdrasil.core.main.factory.DependencyResolutionProviderFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-10-24.
 * Copyright 2016
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DependencyResolutionProviderFactory.class)
public class RemoteDependencyLoaderIntegrationTest {

    @Test
    public void loadDependencies() throws Exception {
        YggdrasilDependency compileDependency = new YggdrasilDependency("test", "test", "test", "test", new File("test"));
        compileDependency.setScope(Scopes.COMPILE);
        YggdrasilDependency providedDependency = new YggdrasilDependency("test", "test", "test", "test", new File("test"));
        providedDependency.setScope(Scopes.PROVIDED);
        YggdrasilDependency excludedDependency =
            new YggdrasilDependency("exclude", "exclude", "exclude", "exclude", new File("exclude"));
        excludedDependency.setScope(Scopes.COMPILE);
        YggdrasilConfiguration configuration = new YggdrasilConfiguration();
        Set<YggdrasilDependency> dependencySet = new HashSet<>();
        dependencySet.add(compileDependency);
        dependencySet.add(excludedDependency);
        dependencySet.add(providedDependency);
        configuration.setDependencies(dependencySet);

        DependencyResolutionProvider providerMock = PowerMockito.mock(DependencyResolutionProvider.class, invocationOnMock -> {
            if (Objects.equals(invocationOnMock.getMethod().getName(), "resolveDependencies")) {
                return dependencySet;
            }
            return invocationOnMock.callRealMethod();
        });
        PowerMockito.mockStatic(DependencyResolutionProviderFactory.class, invocationOnMock -> {
            if (Objects.equals(invocationOnMock.getMethod().getName(), "getDependencyResolvers")) {
                return Collections.singleton(providerMock);
            }
            return invocationOnMock.callRealMethod();
        });

        NestedJarClassLoader nestedJarClassLoader = new NestedJarClassLoader(this.getClass().getClassLoader(), null);
        RemoteDependencyLoader.loadDependencies(nestedJarClassLoader, nestedJarClassLoader, configuration,
            Collections.singleton("exclude"));
    }

}
