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
package io.squark.yggdrasil.maven.provider;


import io.squark.yggdrasil.core.api.exception.DependencyResolutionException;
import io.squark.yggdrasil.core.api.model.YggdrasilDependency;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.internal.impl.DefaultLocalRepositoryProvider;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CollectResult.class)
public class MavenDependencyResolutionProviderIntegrationTest {

    @Test
    public void resolveDependenciesNullTest() throws Exception {
        MavenDependencyResolutionProvider mavenDependencyResolutionProvider = new MavenDependencyResolutionProvider();
        Set set = mavenDependencyResolutionProvider.resolveDependencies(null, false);
        Assert.assertTrue(set.isEmpty());
    }

    @Test
    public void resolveDependenciesTest() throws Exception {
        MavenDependencyResolutionProvider mavenDependencyResolutionProvider =
            Mockito.spy(new MavenDependencyResolutionProvider());

        RepositorySystem repositorySystem = Mockito.mock(RepositorySystem.class);
        Field repositorySystemField = MavenDependencyResolutionProvider.class.getDeclaredField("repositorySystem");
        repositorySystemField.setAccessible(true);
        repositorySystemField.set(mavenDependencyResolutionProvider, repositorySystem);


        DependencyNode dependencyNode =
            new DefaultDependencyNode(new Dependency(new DefaultArtifact("test:test:test:test"), null));
        DependencyNode childNode =
            new DefaultDependencyNode(new Dependency(new DefaultArtifact("child:child:child:child"), null));
        dependencyNode.setChildren(Collections.singletonList(childNode));

        CollectResult collectResult = PowerMockito.mock(CollectResult.class);
        Mockito.when(collectResult.getRoot()).thenReturn(dependencyNode);
        Mockito.when(repositorySystem.collectDependencies(Mockito.any(), Mockito.any())).thenReturn(collectResult);

        Set<YggdrasilDependency> dependencies = new HashSet<>();
        YggdrasilDependency mockDependency = new YggdrasilDependency("test", "test", "test", "test", new File("test"));
        dependencies.add(mockDependency);
        Set<YggdrasilDependency> set = mavenDependencyResolutionProvider.resolveDependencies(dependencies, false);
        Assert.assertEquals(1, set.size());
        Assert.assertEquals("child", set.iterator().next().getGroupId());
        Assert.assertEquals("child", set.iterator().next().getArtifactId());
        Assert.assertEquals("child", set.iterator().next().getVersion());
        Assert.assertEquals("child", set.iterator().next().getExtension());
    }

    @Test
    public void newRepositorySystemSessionTest() throws Exception {
        //Just running through is fine. No asserts necessary here.
        invokeTest(true, true);
        invokeTest(true, false);
        invokeTest(false, false);
    }

    private void invokeTest(boolean setProxy, boolean activeProxy) throws DependencyResolutionException {
        DefaultRepositorySystem repositorySystem = new DefaultRepositorySystem();
        DefaultLocalRepositoryProvider repositoryProvider = new DefaultLocalRepositoryProvider();
        repositoryProvider.addLocalRepositoryManagerFactory(new SimpleLocalRepositoryManagerFactory());
        repositorySystem.setLocalRepositoryProvider(repositoryProvider);
        Settings mavenSettings = new Settings();
        if (setProxy) {
            Proxy proxy = new Proxy();
            proxy.setActive(activeProxy);
            mavenSettings.setProxies(Collections.singletonList(proxy));
        }
        MavenDependencyResolutionProvider provider = new MavenDependencyResolutionProvider() {
            @Override
            protected String getEnv(String key) {
                return "http://mock";
            }
        };
        provider.newRepositorySystemSession(repositorySystem, new LocalRepository("/"), mavenSettings, true);
    }

}