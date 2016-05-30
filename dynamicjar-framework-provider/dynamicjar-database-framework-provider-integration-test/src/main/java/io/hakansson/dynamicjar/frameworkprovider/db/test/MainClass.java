package io.hakansson.dynamicjar.frameworkprovider.db.test;

import org.jboss.weld.environment.se.bindings.Parameters;
import org.jboss.weld.environment.se.contexts.activators.ActivateRequestScope;
import org.jboss.weld.environment.se.events.ContainerInitialized;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-28.
 * Copyright 2016
 */
@Singleton
public class MainClass {

    @Inject
    EntityManager em;

    @ActivateRequestScope
    public void init(@Observes ContainerInitialized event, @Parameters List<String> parameters) {
        em.isOpen();
    }

}
