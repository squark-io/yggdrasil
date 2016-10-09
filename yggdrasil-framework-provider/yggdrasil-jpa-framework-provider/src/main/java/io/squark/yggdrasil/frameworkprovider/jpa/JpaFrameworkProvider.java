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
package io.squark.yggdrasil.frameworkprovider.jpa;

import io.squark.yggdrasil.core.api.FrameworkProvider;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

public class JpaFrameworkProvider implements FrameworkProvider {

    private final Logger logger = InternalLoggerBinder.getLogger(JpaFrameworkProvider.class);

    @Override
    public void provide(@Nullable YggdrasilConfiguration configuration) throws YggdrasilException {
        logger.info("Initializing " + JpaFrameworkProvider.class.getSimpleName() + "...");

        //The only thing we need to to is add some properties.
        System.setProperty(org.hibernate.cfg.AvailableSettings.SCANNER_ARCHIVE_INTERPRETER,
                CustomArchiveDescriptorFactory.class.getName());
        System.setProperty(org.hibernate.jpa.AvailableSettings.TRANSACTION_TYPE, "RESOURCE_LOCAL");

        logger.info(JpaFrameworkProvider.class.getSimpleName() + " initialized.");
    }

    @Override
    public String getName() {
        return JpaFrameworkProvider.class.getSimpleName();
    }

    @Override
    public List<ProviderDependency> runBefore() {
        return Collections.singletonList(new ProviderDependency("WeldFrameworkProvider", true));
    }
}
