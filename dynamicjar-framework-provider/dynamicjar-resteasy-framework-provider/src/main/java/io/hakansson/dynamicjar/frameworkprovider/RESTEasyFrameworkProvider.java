package io.hakansson.dynamicjar.frameworkprovider;

import io.hakansson.dynamicjar.core.api.DynamicJarContext;
import io.hakansson.dynamicjar.core.api.FrameworkProvider;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.frameworkprovider.exception.DynamicJarMultipleResourceException;
import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.spec.ServletContextImpl;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-29.
 * Copyright 2016
 */
public class ResteasyFrameworkProvider implements FrameworkProvider {

    private static final Logger logger = LoggerFactory.getLogger(ResteasyFrameworkProvider.class);

    @Override
    public void provide(DynamicJarConfiguration configuration) throws DynamicJarException {

        BeanManager beanManager =
            (BeanManager) DynamicJarContext.getObject(BeanManager.class.getName());
        if (beanManager != null) {
            logger.info("Found existing BeanManager. Using.");
        }

        UndertowJaxrsServer undertowJaxrsServer = new UndertowJaxrsServer();
        Undertow.Builder serverBuilder = Undertow.builder().addHttpListener(8080, "localhost");

        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
        DeploymentInfo di = undertowJaxrsServer.undertowDeployment(deployment, "/");
        if (beanManager != null) {
            di.addServletContextAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME,
                beanManager);
        }
        di.addInitParameter(
            WeldServletLifecycle.class.getPackage().getName() + ".archive.isolation", "false");
        di.setClassLoader(ResteasyFrameworkProvider.class.getClassLoader()).setContextPath("/")
            .setDeploymentName("My Application")
            .addListener(Servlets.listener(org.jboss.weld.environment.servlet.Listener.class));

        undertowJaxrsServer.deploy(di);

        ServletContextImpl servletContext =
            (ServletContextImpl) deployment.getDefaultContextObjects().get(ServletContext.class);

        if (beanManager == null) {
            beanManager = (BeanManager) servletContext
                .getAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME);
            DynamicJarContext.registerObject(BeanManager.class.getName(), beanManager);
        }
        List<String> resourceClasses = new ArrayList<>();
        String applicationClass = null;
        for (Bean bean : beanManager.getBeans(Object.class, new AnnotationLiteral<Any>() {})) {
            if (bean instanceof ManagedBean) {
                SlimAnnotatedType annotatedType = ((ManagedBean) bean).getAnnotated();
                for (Annotation annotation : annotatedType.getAnnotations()) {
                    if (annotation instanceof Path) {
                        logger.debug("Found resource " + bean.getBeanClass().getSimpleName());
                        resourceClasses.add(bean.getBeanClass().getName());
                    } else if (annotation instanceof ApplicationPath) {
                        if (applicationClass != null) {
                            throw new DynamicJarMultipleResourceException(
                                "Found more than one Application class: " + applicationClass +
                                ", " + bean.getBeanClass().getName());
                        }
                        applicationClass = bean.getBeanClass().getName();
                        logger.debug("Found Application " + bean.getBeanClass().getName());
                    }
                }
            }
        }
        deployment.setResourceClasses(resourceClasses);
        deployment.setApplicationClass(String.class.getName());
        deployment.registration();

        undertowJaxrsServer.start(serverBuilder);

        logger.info(ResteasyFrameworkProvider.class.getSimpleName() + " initialized.");
    }

}
