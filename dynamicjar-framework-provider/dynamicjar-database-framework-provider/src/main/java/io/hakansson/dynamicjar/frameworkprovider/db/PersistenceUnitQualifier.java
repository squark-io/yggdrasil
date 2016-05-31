package io.hakansson.dynamicjar.frameworkprovider.db;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-04.
 * Copyright 2016
 */
@Qualifier
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface PersistenceUnitQualifier {
    String persistenceUnitName() default "";

    @SuppressWarnings("ClassExplicitlyAnnotation")
    public class PersistenceUnitQualifierImpl implements PersistenceUnitQualifier {

        String persistenceUnitName;

        public PersistenceUnitQualifierImpl(String persistenceUnitName) {
            this.persistenceUnitName = persistenceUnitName;
        }

        @Override
        public String persistenceUnitName() {
            return persistenceUnitName;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return PersistenceUnitQualifier.class;
        }
    }
}
