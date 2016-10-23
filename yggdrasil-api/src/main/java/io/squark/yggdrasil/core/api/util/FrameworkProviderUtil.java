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
package io.squark.yggdrasil.core.api.util;

import io.squark.yggdrasil.core.api.FrameworkProvider;
import io.squark.yggdrasil.core.api.exception.FrameworkProviderException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Erik Håkansson on 2016-05-31.
 * WirelessCar
 */
public class FrameworkProviderUtil {

    public static void validateDependencies(List<FrameworkProvider> providerList) throws FrameworkProviderException {
        for (FrameworkProvider provider : providerList) {
            Set<FrameworkProvider.ProviderDependency> flatDependencies = new HashSet<>();
            flatDependencies.addAll(provider.runBefore());
            flatDependencies.addAll(provider.runAfter());
            for (FrameworkProvider.ProviderDependency dependency : flatDependencies) {
                if (dependency.optional) {
                    continue;
                }
                boolean found = false;
                for (FrameworkProvider dependencyProvider : providerList) {
                    if (dependency.name.equals(dependencyProvider.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new FrameworkProviderException("FrameworkProvider " + provider.getName() + " has a dependency on FrameworkProvider " + dependency.name + " which was not found");
                }
            }
        }
    }
}
