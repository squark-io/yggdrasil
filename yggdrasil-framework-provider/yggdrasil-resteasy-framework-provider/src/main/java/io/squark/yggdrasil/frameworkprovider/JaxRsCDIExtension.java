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
