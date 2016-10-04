package io.squark.dynamicjar.frameworkprovider.jpa;

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

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-17.
 * Copyright 2016
 */
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