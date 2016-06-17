package io.hakansson.dynamicjar.frameworkprovider.jpa;

import io.hakansson.dynamicjar.logging.api.InternalLoggerBinder;
import org.apache.commons.lang3.StringUtils;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Erik Håkansson on 2016-05-31.
 * WirelessCar
 */
public class JpaCDIServices implements JpaInjectionServices {

    private static final Logger logger = InternalLoggerBinder.getLogger(JpaCDIServices.class);

    private static Map<String, ResourceReference<EntityManagerFactory>> entityManagerFactories = new HashMap<>();

    private static String getPersistenceUnitNameFromPersistenceContext(InjectionPoint injectionPoint) {
        PersistenceContext context = injectionPoint.getAnnotated().getAnnotation(PersistenceContext.class);
        String persistenceUnitName = context != null ? context.unitName() : null;
        if (StringUtils.isEmpty(persistenceUnitName)) persistenceUnitName = null;
        return persistenceUnitName;
    }

    private static String getPersistenceUnitNameFromPersistenceUnit(InjectionPoint injectionPoint) {
        PersistenceUnit persistenceUnit = injectionPoint.getAnnotated().getAnnotation(PersistenceUnit.class);
        String persistenceUnitName = persistenceUnit != null ? persistenceUnit.unitName() : null;
        if (StringUtils.isEmpty(persistenceUnitName)) persistenceUnitName = null;
        return persistenceUnitName;
    }

    private static Properties getPropertiesFromPersistenceContext(InjectionPoint injectionPoint) {
        PersistenceContext persistenceContext = injectionPoint.getAnnotated().getAnnotation(PersistenceContext.class);
        if (persistenceContext != null) {
            Properties properties = new Properties();
            for (PersistenceProperty property : persistenceContext.properties()) {
                properties.put(property.name(), property.value());
            }
            return properties;
        }
        return null;
    }

    @Override
    public ResourceReferenceFactory<EntityManager> registerPersistenceContextInjectionPoint(InjectionPoint injectionPoint) {
        return () -> {
            String persistenceUnitName = getPersistenceUnitNameFromPersistenceContext(injectionPoint);

            logger.info("Getting EntityManager for Persistence Unit with name " + persistenceUnitName);
            ResourceReference<EntityManagerFactory> entityManagerFactoryRef = getEntityManagerFactoryRef(persistenceUnitName);
            Map properties = getPropertiesFromPersistenceContext(injectionPoint);
            if (properties != null) {
                return new EntityManagerResourceReference(entityManagerFactoryRef.getInstance().createEntityManager(properties),
                        persistenceUnitName);
            } else {
                return new EntityManagerResourceReference(entityManagerFactoryRef.getInstance().createEntityManager(),
                        persistenceUnitName);
            }
        };
    }

    @Override
    public ResourceReferenceFactory<EntityManagerFactory> registerPersistenceUnitInjectionPoint(InjectionPoint injectionPoint) {
        return () -> {
            String persistenceUnitName = getPersistenceUnitNameFromPersistenceUnit(injectionPoint);
            logger.info("Getting EntityManagerFactory for Persistence Unit with name " + persistenceUnitName);
            return getEntityManagerFactoryRef(persistenceUnitName);
        };
    }

    /**
     * @param injectionPoint
     * @deprecated
     */
    @Override
    public EntityManager resolvePersistenceContext(InjectionPoint injectionPoint) {
        return registerPersistenceContextInjectionPoint(injectionPoint).createResource().getInstance();
    }

    /**
     * @param injectionPoint
     * @deprecated
     */
    @Override
    public EntityManagerFactory resolvePersistenceUnit(InjectionPoint injectionPoint) {
        return registerPersistenceUnitInjectionPoint(injectionPoint).createResource().getInstance();
    }

    @Override
    public void cleanup() {
        logger.info("Cleaning up JPA");
        for (ResourceReference resourceReference : entityManagerFactories.values()) {
            resourceReference.release();
        }
    }

    ResourceReference<EntityManagerFactory> getEntityManagerFactoryRef(String persistenceUnitName) {
        ResourceReference<EntityManagerFactory> entityManagerFactoryRef = entityManagerFactories.get(persistenceUnitName);
        if (entityManagerFactoryRef == null) {
            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
            entityManagerFactoryRef = new EntityManagerFactoryResourceReference(entityManagerFactory, persistenceUnitName);
            entityManagerFactories.put(persistenceUnitName, entityManagerFactoryRef);
        }

        return entityManagerFactoryRef;
    }

    static class EntityManagerFactoryResourceReference implements ResourceReference<EntityManagerFactory> {

        private EntityManagerFactory entityManagerFactory;
        private String name;

        public EntityManagerFactoryResourceReference(EntityManagerFactory entityManagerFactory, String name) {
            this.entityManagerFactory = entityManagerFactory;
            this.name = name;
        }

        @Override
        public EntityManagerFactory getInstance() {
            return entityManagerFactory;
        }

        @Override
        public void release() {
            if (entityManagerFactory != null) {
                logger.debug("Closing EntityManagerFactory with name " + name);
                entityManagerFactory.close();
            }
            entityManagerFactory = null;
        }
    }

    private static class EntityManagerResourceReference implements ResourceReference<EntityManager> {

        private EntityManager entityManager;
        private String name;

        public EntityManagerResourceReference(EntityManager entityManager, String name) {
            this.entityManager = entityManager;
            this.name = name;
        }

        @Override
        public EntityManager getInstance() {
            return entityManager;
        }

        @Override
        public void release() {
            if (entityManager != null) {
                logger.debug("Closing EntityManager with name " + name);
                entityManager.close();
            }
            entityManager = null;
        }
    }
}
