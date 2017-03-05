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
package io.squark.yggdrasil.frameworkprovider;

import io.squark.yggdrasil.core.api.YggdrasilServletDeploymentInfo;
import io.squark.yggdrasil.core.api.YggdrasilServletInfo;
import io.squark.yggdrasil.core.api.ServletProvider;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.ProviderConfiguration;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.frameworkprovider.exception.YggdrasilMultipleResourceException;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.List;

public class JaxRsServletProvider implements ServletProvider {

  private static final String PROPERTY_MAPPING = "mapping";
  private static final String DEFAULT_MAPPING = "/rest/*";
  private final Logger logger = LoggerFactory.getLogger(JaxRsServletProvider.class);

  @Override
  public YggdrasilServletDeploymentInfo initialize(BeanManager beanManager, YggdrasilConfiguration configuration) throws YggdrasilException {

    YggdrasilServletDeploymentInfo yggdrasilServletDeploymentInfo = new YggdrasilServletDeploymentInfo();

    String mapping = DEFAULT_MAPPING;
    if (configuration.getProviderConfigurations().isPresent()) {
      for (ProviderConfiguration providerConfiguration : configuration.getProviderConfigurations().get()) {
        if (providerConfiguration.getIdentifier().equals(JaxRsServletProvider.class.getSimpleName())) {
          mapping = (String) providerConfiguration.getProperties().getOrDefault(PROPERTY_MAPPING, DEFAULT_MAPPING);
          break;
        }
      }
    }

    String prefix = null;
    if (!mapping.equals("/*")) prefix = mapping.substring(0, mapping.length() - 2);

    yggdrasilServletDeploymentInfo
      .addListener(org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap.class).addServlet(
      YggdrasilServletInfo.servlet("JsxRsServlet", HttpServlet30Dispatcher.class).addMapping(mapping).setLoadOnStartup(1)
        .setAsyncSupported(true)).setEagerFilterInit(true);
    if (prefix != null) yggdrasilServletDeploymentInfo.addInitParameter("resteasy.servlet.mapping.prefix", prefix);


    ResteasyCdiExtension resteasyCdiExtension = getBean(beanManager, ResteasyCdiExtension.class);
    if (resteasyCdiExtension == null) {
      throw new YggdrasilException("Failed to load " + ResteasyCdiExtension.class.getName() + " bean");
    }

    JaxRsCDIExtension jaxRsCDIExtension = getBean(beanManager, JaxRsCDIExtension.class);
    if (jaxRsCDIExtension == null) {
      throw new YggdrasilException("Failed to get JaxRsCDIExtension bean");
    }

    ResteasyDeployment deployment = new ResteasyDeployment();
    deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
    List<String> applications;
    if ((applications = jaxRsCDIExtension.getApplications()) != null && applications.size() > 0) {
      if (applications.size() > 1) {
        throw new YggdrasilMultipleResourceException("Multiple Application classes: " + jaxRsCDIExtension.getApplications());
      }
      logger.debug("Found Application class " + applications.get(0));
      deployment.setApplicationClass(applications.get(0));
    }

    List<Object> resourceInstances = new ArrayList<>();
    for (Class<?> clazz : resteasyCdiExtension.getResources()) {
      logger.debug("Found resource class " + clazz.getName());
      Object bean = getBean(beanManager, clazz);
      resourceInstances.add(bean);
    }
    deployment.setResources(resourceInstances);

    List<String> providers;
    if ((providers = jaxRsCDIExtension.getProviders()) != null && providers.size() > 0) {
      if (logger.isDebugEnabled()) {
        logger.debug("Found provider classes: " + concatListOfStrings(providers, 5));
      }
      deployment.setProviderClasses(providers);
    }
    yggdrasilServletDeploymentInfo.addContextObject(ResteasyDeployment.class.getName(), deployment);

    yggdrasilServletDeploymentInfo.addServletContextAttribute(ResteasyDeployment.class.getName(), deployment);

    logger.info(JaxRsServletProvider.class.getSimpleName() + " initialized.");

    return yggdrasilServletDeploymentInfo;
  }

  @Override
  public void postInitialize(YggdrasilServletDeploymentInfo yggdrasilServletDeploymentInfo, YggdrasilConfiguration configuration) {
    ResteasyDeployment deployment = (ResteasyDeployment) yggdrasilServletDeploymentInfo.getContextObject(ResteasyDeployment.class.getName());
    deployment.registration();
  }

  private String concatListOfStrings(List<String> list, int max) {
    StringBuilder builder = new StringBuilder("[");
    String comma = "";
    for (int i = 0; i < list.size() && i < max; i++) {
      builder.append(comma).append(list.get(i));
      comma = ", ";
    }
    if (list.size() > max) {
      builder.append("and ").append(list.size() - max).append(" more...");
    }
    builder.append("]");
    return builder.toString();
  }
}
