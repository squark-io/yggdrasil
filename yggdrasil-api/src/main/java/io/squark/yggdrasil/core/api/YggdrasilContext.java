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
package io.squark.yggdrasil.core.api;

import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class YggdrasilContext {
    private static Map<String, Object> registeredObjects = new HashMap<>();
    private static URL overriddenLibraryPath;
    private static YggdrasilConfiguration configuration;

    public static void registerObject(String name, Object object) {
        registeredObjects.put(name, object);
    }

    public static Object getObject(String name) {
        return registeredObjects.get(name);
    }

    public static <T> T getObject(Class<T> type) {
        return type.cast(registeredObjects.get(type.getName()));
    }

    public static void overrideLibraryPath(URL resource) {
        overriddenLibraryPath = resource;
    }

    public static URL getOverriddenLibraryPath() {
        return overriddenLibraryPath;
    }

    public static void setConfiguration(YggdrasilConfiguration configuration) {
        YggdrasilContext.configuration = configuration;
    }

    public static YggdrasilConfiguration getConfiguration() {
        return configuration;
    }
}
