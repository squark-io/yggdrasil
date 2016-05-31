package io.hakansson.dynamicjar.frameworkprovider.db;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.internal.EntityManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.AnnotationLiteral;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-04.
 * Copyright 2016
 */
class EntityManagerBean implements Bean<EntityManagerImpl> {

    private Logger logger = LoggerFactory.getLogger(EntityManagerBean.class);

    private PersistenceInjectionExtension persistenceInjectionExtension;
    private InjectionTarget<EntityManagerImpl> it;
    private PersistenceUnitQualifier persistenceUnitQualifier;

    public EntityManagerBean(PersistenceInjectionExtension persistenceInjectionExtension, InjectionTarget<EntityManagerImpl> it,
                             PersistenceUnitQualifier persistenceUnitQualifier)
    {
        this.persistenceInjectionExtension = persistenceInjectionExtension;
        this.it = it;
        this.persistenceUnitQualifier = persistenceUnitQualifier;
    }

    @Override
    public Class<?> getBeanClass() {
        return EntityManager.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return it.getInjectionPoints();
    }


    @Override
    public String getName() {
        return "EntityManager";
    }


    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<>();
        qualifiers.add(new AnnotationLiteral<Default>() {
        });
        qualifiers.add(new AnnotationLiteral<Any>() {
        });
        qualifiers.add(persistenceUnitQualifier);
        return qualifiers;
    }


    @Override

    public Class<? extends Annotation> getScope() {
        return RequestScoped.class;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }


    @Override
    public Set<Type> getTypes() {
        Set<Type> types = new HashSet<>();
        types.add(EntityManagerImpl.class);
        types.add(EntityManager.class);
        types.add(Object.class);

        return types;
    }


    @Override
    public boolean isAlternative() {
        return false;
    }


    @Override
    public boolean isNullable() {
        return false;
    }


    @Override
    public EntityManagerImpl create(CreationalContext<EntityManagerImpl> ctx) {
        String persistenceUnitName = persistenceUnitQualifier.persistenceUnitName();
        if (StringUtils.isEmpty(persistenceUnitName)) {
            //May be empty but not null. If so, we want it to be null instead.
            persistenceUnitName = null;
        }
        logger.debug("Creating EntityManager for persistence unit \"" + persistenceUnitName + '"');
        EntityManagerFactory factory = persistenceInjectionExtension.getEntityManagerFactories().get(persistenceUnitName);
        EntityManagerImpl instance = (EntityManagerImpl) factory.createEntityManager();
        it.inject(instance, ctx);
        it.postConstruct(instance);
        return instance;
    }

    @Override
    public void destroy(EntityManagerImpl instance, CreationalContext<EntityManagerImpl> ctx)
    {
        if (logger.isDebugEnabled()) {
            String unitName = persistenceUnitQualifier.persistenceUnitName();
            if (StringUtils.isEmpty(unitName)) unitName = null;
            logger.debug("Destroying EntityManager \"" + unitName + '"');
        }
        it.preDestroy(instance);
        if (instance.isOpen()) {
            instance.close();
        }
        it.dispose(instance);
        ctx.release();
    }
}
