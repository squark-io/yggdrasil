package io.squark.ask.frameworkprovider;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-04-30.
 * Copyright 2016
 */
public class JaxRsCDIExtension implements Extension {

    private List<String> resources;
    private List<String> applications;
    private List<String> providers;

    public <T> void observeResources(
        @WithAnnotations({Path.class}) @Observes ProcessAnnotatedType<T> event,
        BeanManager beanManager) {
        AnnotatedType<T> annotatedType = event.getAnnotatedType();

        addResource(annotatedType.getJavaClass());
    }

    public <T extends Application> void observeApplications(@Observes ProcessAnnotatedType<T> event,
        BeanManager beanManager) {
        AnnotatedType<T> annotatedType = event.getAnnotatedType();

        addApplication(annotatedType.getJavaClass());
    }

    public <T> void observeProviders(
        @WithAnnotations({Provider.class}) @Observes ProcessAnnotatedType<T> event,
        BeanManager beanManager) {

        AnnotatedType<T> annotatedType = event.getAnnotatedType();

        addProvider(annotatedType.getJavaClass());
    }

    private <T extends Application> void addApplication(Class<T> javaClass) {
        if (applications == null) {
            applications = new ArrayList<>();
        }
        applications.add(javaClass.getName());
    }

    private <T> void addProvider(Class<T> javaClass) {
        if (providers == null) {
            providers = new ArrayList<>();
        }
        providers.add(javaClass.getName());
    }

    private <T> void addResource(Class<T> javaClass) {
        if (resources == null) {
            resources = new ArrayList<>();
        }
        resources.add(javaClass.getName());
    }

    public List<String> getResources() {
        return resources;
    }

    public List<String> getApplications() {
        return applications;
    }

    public List<String> getProviders() {
        return providers;
    }
}
