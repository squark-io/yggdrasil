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
package io.squark.yggdrasil.core.main.factory;

import io.squark.yggdrasil.core.api.DependencyResolutionProvider;
import io.squark.yggdrasil.core.api.exception.DependencyResolutionException;
import io.squark.yggdrasil.core.main.Yggdrasil;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import io.squark.nestedjarclassloader.NestedJarClassLoader;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

public class DependencyResolutionProviderFactory {

    private static Logger logger = InternalLoggerBinder.getLogger(Yggdrasil.class);

    public static Collection<DependencyResolutionProvider> getDependencyResolvers(NestedJarClassLoader helperClassloader) throws
            DependencyResolutionException
    {

        ServiceLoader<DependencyResolutionProvider> loader = ServiceLoader.load(DependencyResolutionProvider.class,
                helperClassloader);
        Collection<DependencyResolutionProvider> providers = new ArrayList<>();
        List<String> providerNames = null;
        boolean isDebug = logger.isDebugEnabled();
        if (isDebug) {
            providerNames = new ArrayList<>();
        }
        for (DependencyResolutionProvider provider : loader) {
            providers.add(provider);
            if (isDebug) {
                providerNames.add(provider.getClass().getName());
            }
        }
        logger.debug("Found providers " + providerNames);

        return providers;
    }
}
