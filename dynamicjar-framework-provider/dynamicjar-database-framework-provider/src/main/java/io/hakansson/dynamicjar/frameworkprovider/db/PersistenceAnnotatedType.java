package io.hakansson.dynamicjar.frameworkprovider.db;

import org.jboss.weld.util.annotated.ForwardingAnnotatedType;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import java.util.Set;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-04.
 * Copyright 2016
 */
public class PersistenceAnnotatedType<T> extends ForwardingAnnotatedType<T> {

    private final AnnotatedType<T> delegate;
    private final Set<AnnotatedField<? super T>> fields;

    public PersistenceAnnotatedType(AnnotatedType<T> delegate, Set<AnnotatedField<? super T>> fields) {
        this.delegate = delegate;
        this.fields = fields;
    }

    @Override
    public AnnotatedType<T> delegate() {
        return delegate;
    }

    @Override
    public Set<AnnotatedField<? super T>> getFields() {
        return (fields == null) ? delegate.getFields() : fields;
    }
}
