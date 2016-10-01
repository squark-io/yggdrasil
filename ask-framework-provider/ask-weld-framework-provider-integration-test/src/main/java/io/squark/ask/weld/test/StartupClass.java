package io.squark.ask.weld.test;

import org.jboss.weld.environment.se.bindings.Parameters;
import org.jboss.weld.environment.se.events.ContainerInitialized;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-02-27.
 * Copyright 2016
 */
@Singleton
public class StartupClass {

    @Inject
    InjectedClass injectedClass;

    public void init(@Observes ContainerInitialized event, @Parameters List<String> parameters) {
        injectedClass.sayHello();
    }

}
