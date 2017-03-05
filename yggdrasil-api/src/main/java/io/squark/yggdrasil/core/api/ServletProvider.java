/*
 * Copyright (c) 2017 Erik Håkansson, http://squark.io
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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Set;

public interface ServletProvider {

  YggdrasilServletDeploymentInfo initialize(BeanManager beanManager, YggdrasilConfiguration yggdrasilConfiguration) throws YggdrasilException;

  default <T> T getBean(BeanManager manager, Class<T> type) {
    Set<Bean<?>> beans = manager.getBeans(type);
    Bean<?> bean = manager.resolve(beans);
    if (bean == null) {
      return null;
    }
    CreationalContext<?> context = manager.createCreationalContext(bean);
    return type.cast(manager.getReference(bean, type, context));
  }

  void postInitialize(YggdrasilServletDeploymentInfo yggdrasilServletDeploymentInfo, YggdrasilConfiguration configuration);
}
