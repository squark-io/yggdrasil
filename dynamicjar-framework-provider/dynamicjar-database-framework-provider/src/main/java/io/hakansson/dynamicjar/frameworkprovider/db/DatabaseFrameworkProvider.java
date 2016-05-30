package io.hakansson.dynamicjar.frameworkprovider.db;

import io.hakansson.dynamicjar.core.api.DynamicJarContext;
import io.hakansson.dynamicjar.core.api.FrameworkProvider;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-28.
 * Copyright 2016
 */
public class DatabaseFrameworkProvider implements FrameworkProvider {

    private final Logger logger = LoggerFactory.getLogger(DatabaseFrameworkProvider.class);
    private static EntityManagerFactory entityManagerFactory;

    private final Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            logger.info("Shutting down EntityManagerFactory");
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
                logger.debug("EntityManagerFactory closed");
            } else {
                logger.warn("Open EntityManagerFactory not found.");
            }
        }
    };

    @Override
    public void provide(@Nullable DynamicJarConfiguration configuration) throws DynamicJarException {
        //TODO: See PersistenceXMLParser row 111

        System.setProperty(org.hibernate.cfg.AvailableSettings.SCANNER_ARCHIVE_INTERPRETER,
                CustomArchiveDescriptorFactory.class.getName());

        logger.info("Initializing " + DatabaseFrameworkProvider.class.getSimpleName() + "...");
        entityManagerFactory = Persistence.createEntityManagerFactory("persistenceUnit");

        BeanManager beanManager = DynamicJarContext.getObject(BeanManager.class);
        if (beanManager != null) {

            String test = "test";
        }

        Runtime.getRuntime().addShutdownHook(shutdownHook);

        logger.info(DatabaseFrameworkProvider.class.getSimpleName() + " intialized.");
    }

    @Produces
    @RequestScoped
    public EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }
}
