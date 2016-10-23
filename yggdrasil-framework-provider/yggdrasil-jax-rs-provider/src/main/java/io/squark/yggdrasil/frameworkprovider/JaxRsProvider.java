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
package io.squark.yggdrasil.frameworkprovider;

import io.squark.yggdrasil.core.api.YggdrasilContext;
import io.squark.yggdrasil.core.api.FrameworkProvider;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.core.api.model.ProviderConfiguration;
import io.squark.yggdrasil.frameworkprovider.exception.YggdrasilMultipleResourceException;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.spec.ServletContextImpl;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;
import org.slf4j.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JaxRsProvider implements FrameworkProvider {

    private static final Logger logger = InternalLoggerBinder.getLogger(JaxRsProvider.class);
    private static final String DEFAULT_PORT = "8080";
    private static final String PROPERTY_PORT = "port";
    private int port = Integer.valueOf(DEFAULT_PORT);

    @Override
    public void provide(YggdrasilConfiguration configuration) throws YggdrasilException {

        logger.info("Initializing Resteasy ...");
        System.setProperty("org.jboss.logging.provider", "slf4j");

        configuration.getProviderConfigurations().ifPresent(providerConfigurations -> {
            for (ProviderConfiguration providerConfiguration : providerConfigurations) {
                if (providerConfiguration.getIdentifier().equals(JaxRsProvider.class.getSimpleName())) {
                    port = Integer.valueOf(
                        (String) providerConfiguration.getProperties().getOrDefault(PROPERTY_PORT, DEFAULT_PORT));
                    break;
                }
            }
        });

        BeanManager beanManager = (BeanManager) YggdrasilContext.getObject(BeanManager.class.getName());
        if (beanManager != null) {
            logger.info("Found existing BeanManager. Using.");
        }

        UndertowJaxrsServer undertowJaxrsServer = new UndertowJaxrsServer();

        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
        DeploymentInfo di = undertowJaxrsServer.undertowDeployment(deployment, "/");
        di.addInitParameter(WeldServletLifecycle.class.getPackage().getName() + ".archive.isolation", "false");
        if (beanManager != null) {
            di.addServletContextAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, beanManager);
        }
        di.setClassLoader(JaxRsProvider.class.getClassLoader()).setContextPath("/")
            .setDeploymentName("My Application")
            .addListener(Servlets.listener(org.jboss.weld.environment.servlet.Listener.class))
            .addListener(Servlets.listener(org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap.class));

        undertowJaxrsServer.deploy(di);

        ServletContextImpl servletContext =
            (ServletContextImpl) deployment.getDefaultContextObjects().get(ServletContext.class);

        if (beanManager == null) {
            beanManager = (BeanManager) servletContext.getAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME);
            YggdrasilContext.registerObject(BeanManager.class.getName(), beanManager);
        }

        Undertow.Builder serverBuilder = Undertow.builder().addHttpListener(port, "localhost");
        undertowJaxrsServer.start(serverBuilder);

        JaxRsCDIExtension jaxRsCDIExtension = getBean(beanManager, JaxRsCDIExtension.class);
        if (jaxRsCDIExtension == null) {
            throw new YggdrasilException("Failed to get JaxRsCDIExtension bean");
        }
        List<String> applications;
        if ((applications = jaxRsCDIExtension.getApplications()) != null && applications.size() > 0) {
            if (applications.size() > 1) {
                throw new YggdrasilMultipleResourceException(
                    "Multiple Application classes: " + jaxRsCDIExtension.getApplications());
            }
            logger.debug("Found Application class " + applications.get(0));
            deployment.setApplicationClass(applications.get(0));
        }
        List<String> resources;

        if ((resources = jaxRsCDIExtension.getResources()) != null && resources.size() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found resource classes: " + concatListOfStrings(resources, 5));
            }
            deployment.setResourceClasses(resources);
        }

        List<String> providers;
        if ((providers = jaxRsCDIExtension.getProviders()) != null && providers.size() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found provider classes: " + concatListOfStrings(providers, 5));
            }
            deployment.setProviderClasses(providers);
        }
        deployment.registration();

        logger.info(JaxRsProvider.class.getSimpleName() + " initialized.");
    }

    @Override
    public String getName() {
        return JaxRsProvider.class.getSimpleName();
    }

    private <T> T getBean(BeanManager manager, Class<T> type) {
        Set<Bean<?>> beans = manager.getBeans(type);
        Bean<?> bean = manager.resolve(beans);
        if (bean == null) {
            return null;
        }
        CreationalContext<?> context = manager.createCreationalContext(bean);
        return type.cast(manager.getReference(bean, type, context));
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

    @Override
    public List<ProviderDependency> runAfter() {
        return Collections.singletonList(new ProviderDependency("WeldFrameworkProvider", true));
    }
}
