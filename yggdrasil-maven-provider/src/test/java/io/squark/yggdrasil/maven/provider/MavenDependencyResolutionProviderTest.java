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
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.internal.impl.DefaultLocalRepositoryProvider;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class MavenDependencyResolutionProviderTest {

    @Test
    public void testNewRepositorySystemSessionWithProxy() throws Exception {
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