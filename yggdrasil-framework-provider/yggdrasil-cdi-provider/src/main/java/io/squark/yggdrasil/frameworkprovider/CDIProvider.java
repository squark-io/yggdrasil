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
package io.squark.yggdrasil.frameworkprovider;

import io.squark.yggdrasil.core.api.YggdrasilContext;
import io.squark.yggdrasil.core.api.FrameworkProvider;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.BeanManager;

public class CDIProvider implements FrameworkProvider {

    private static final Logger logger = InternalLoggerBinder.getLogger(CDIProvider.class);

    @Override
    public void provide(YggdrasilConfiguration configuration) throws YggdrasilException {
        logger.info("Initializing Weld container...");
        System.setProperty("org.jboss.logging.provider", "slf4j");
        Weld weld = new Weld();
        weld.setClassLoader(CDIProvider.class.getClassLoader());
        WeldContainer container = weld.initialize();
        YggdrasilContext.registerObject(BeanManager.class.getName(), container.getBeanManager());
        logger.info(CDIProvider.class.getSimpleName() + " initialized.");
    }

    @Override
    public String getName() {
        return CDIProvider.class.getSimpleName();
    }
}
