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

import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.*;
import java.lang.annotation.Annotation;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class JpaCDIServicesTest {

    @Mock
    InjectionPoint injectionPoint;

    @Mock
    Annotated annotated;

    @Mock
    EntityManagerFactory entityManagerFactory;

    @Spy
    JpaCDIServices jpaCDIServices = new JpaCDIServices() {
        @Override
        ResourceReference<EntityManagerFactory> getEntityManagerFactoryRef(String persistenceUnitName) {
            return new EntityManagerFactoryResourceReference(entityManagerFactory, persistenceUnitName);
        }
    };

    @Before
    public void setup() {
        initMocks(this);
        when(injectionPoint.getAnnotated()).thenReturn(annotated);
    }

    @Test
    public void registerPersistenceContextInjectionPoint() throws Exception {
        when(annotated.getAnnotation(PersistenceContext.class)).thenReturn(new PersistenceContext() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return PersistenceContext.class;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public String unitName() {
                return "mockName";
            }

            @Override
            public PersistenceContextType type() {
                return null;
            }

            @Override
            public SynchronizationType synchronization() {
                return null;
            }

            @Override
            public PersistenceProperty[] properties() {
                return new PersistenceProperty[0];
            }
        });
        ResourceReferenceFactory<EntityManager> factory = jpaCDIServices.registerPersistenceContextInjectionPoint(injectionPoint);
        factory.createResource();
    }

    @Test
    public void registerPersistenceUnitInjectionPoint() throws Exception {
        when(annotated.getAnnotation(PersistenceUnit.class)).thenReturn(new PersistenceUnit() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return PersistenceUnit.class;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public String unitName() {
                return "mockName";
            }
        });
        ResourceReferenceFactory<EntityManagerFactory> factory = jpaCDIServices.registerPersistenceUnitInjectionPoint(injectionPoint);
        factory.createResource();
    }

    @Test
    public void resolvePersistenceContext() throws Exception {
        when(annotated.getAnnotation(PersistenceContext.class)).thenReturn(new PersistenceContext() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return PersistenceContext.class;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public String unitName() {
                return "mockName";
            }

            @Override
            public PersistenceContextType type() {
                return null;
            }

            @Override
            public SynchronizationType synchronization() {
                return null;
            }

            @Override
            public PersistenceProperty[] properties() {
                return new PersistenceProperty[0];
            }
        });
        jpaCDIServices.resolvePersistenceContext(injectionPoint);
    }

    @Test
    public void resolvePersistenceUnit() throws Exception {
        when(annotated.getAnnotation(PersistenceUnit.class)).thenReturn(new PersistenceUnit() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return PersistenceUnit.class;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public String unitName() {
                return "mockName";
            }
        });
        jpaCDIServices.resolvePersistenceUnit(injectionPoint);
    }

}