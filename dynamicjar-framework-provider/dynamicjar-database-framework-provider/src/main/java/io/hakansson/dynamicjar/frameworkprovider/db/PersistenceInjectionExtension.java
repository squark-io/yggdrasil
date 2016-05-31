package io.hakansson.dynamicjar.frameworkprovider.db;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.internal.EntityManagerImpl;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedField;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedType;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.SharedObjectCache;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created by Erik Håkansson on 2016-06-02.
 * WirelessCar
 */
public class PersistenceInjectionExtension implements Extension, Service {

    private InjectionTarget<EntityManagerImpl> entityManagerInjectionTarget;
    private HashMap<String, EntityManagerBean> entityManagerBeanStore = new HashMap<>();
    private Map<String, EntityManagerFactory> entityManagerFactories = new HashMap<>();

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
        AnnotatedType<EntityManagerImpl> annotatedType = beanManager.createAnnotatedType(EntityManagerImpl.class);
        //use this to instantiate the class and inject dependencies
        entityManagerInjectionTarget = beanManager.createInjectionTarget(annotatedType);
    }

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event, BeanManager beanManager) {
        Set<AnnotatedField<? super T>> annotatedFieldSet = event.getAnnotatedType().getFields();
        List<AnnotatedField<? super T>> fieldsToRemove = new ArrayList<>();
        List<AnnotatedField<? super T>> fieldsToAdd = new ArrayList<>();
        BeanManagerImpl beanManagerImpl = BeanManagerProxy.tryUnwrap(beanManager);
        SharedObjectCache sharedObjectCache = SharedObjectCache.instance(beanManagerImpl);

        //TODO: also check constructors and methods.
        //TODO: Also check for factories with @PersistenceUnit

        for (AnnotatedField<? super T> field : annotatedFieldSet) {
            PersistenceContext persistenceContext = field.getAnnotation(PersistenceContext.class);
            if (persistenceContext != null && field.getBaseType() == EntityManager.class) {
                String persistenceUnitName = persistenceContext.unitName();
                if (!field.isAnnotationPresent(Inject.class)) {

                    fieldsToRemove.add(field);
                    Set<Annotation> newAnnotations = new HashSet<>(field.getAnnotations());
                    newAnnotations.add(new AnnotationLiteral<Inject>() {
                    });
                    newAnnotations.add(new PersistenceUnitQualifier.PersistenceUnitQualifierImpl(persistenceUnitName));
                    UnbackedAnnotatedType<? super T> unbackedAnnotatedType = UnbackedAnnotatedType.additionalAnnotatedType(
                            beanManagerImpl.getContextId(), field.getDeclaringType(), beanManagerImpl.getId(), null,
                            sharedObjectCache);
                    UnbackedAnnotatedField<? super T> newField = new UnbackedAnnotatedField<>(field.getBaseType(),
                            field.getTypeClosure(), newAnnotations, field.getJavaMember(), unbackedAnnotatedType);
                    fieldsToAdd.add(newField);
                }
            }
        }
        if (fieldsToAdd.size() > 0) {
            Set<AnnotatedField<? super T>> fields = new HashSet<>();
            fields.addAll(event.getAnnotatedType().getFields());
            fields.removeAll(fieldsToRemove);
            fields.addAll(fieldsToAdd);
            AnnotatedType<T> annotatedType = new PersistenceAnnotatedType<T>(event.getAnnotatedType(), fields);
            event.setAnnotatedType(annotatedType);
        }
        beanManager.createAnnotatedType(EntityManager.class);
    }

    <T, X> void observerInjectionPoint(@Observes ProcessInjectionPoint<T, X> event, BeanManager beanManager) {
        InjectionPoint injectionPoint = event.getInjectionPoint();
        if (injectionPoint.getAnnotated() instanceof UnbackedAnnotatedField) {
            PersistenceUnitQualifier persistenceUnitQualifier;
            if ((persistenceUnitQualifier = injectionPoint.getAnnotated().getAnnotation(
                    PersistenceUnitQualifier.class)) != null)
            {
                String persistenceUnitName = persistenceUnitQualifier.persistenceUnitName();
                //May be empty string instead of null, but should really be null.
                if (StringUtils.isEmpty(persistenceUnitName)) {
                    persistenceUnitName = null;
                }
                if (!entityManagerFactories.containsKey(persistenceUnitName)) {
                    entityManagerFactories.put(persistenceUnitName, Persistence.createEntityManagerFactory(persistenceUnitName));
                }
                if (!entityManagerBeanStore.containsKey(persistenceUnitName)) {
                    EntityManagerBean entityManagerBean = new EntityManagerBean(this, entityManagerInjectionTarget,
                            persistenceUnitQualifier);
                    entityManagerBeanStore.put(persistenceUnitName, entityManagerBean);
                    BeanManagerImpl beanManagerImpl = BeanManagerProxy.tryUnwrap(beanManager);
                    beanManagerImpl.addBean(entityManagerBean);
                }
            }
        }
    }

    @Override
    public void cleanup() {
        for (EntityManagerFactory factory : entityManagerFactories.values()) {
            if (factory.isOpen()) {
                factory.close();
            }
        }
    }

    public Map<String, EntityManagerFactory> getEntityManagerFactories() {
        return entityManagerFactories;
    }

}
