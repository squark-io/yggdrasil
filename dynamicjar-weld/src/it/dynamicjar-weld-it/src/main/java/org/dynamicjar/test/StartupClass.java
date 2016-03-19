package org.dynamicjar.test;

import org.jboss.weld.environment.se.bindings.Parameters;
import org.jboss.weld.environment.se.events.ContainerInitialized;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static java.lang.System.getProperties;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-27.
 * Copyright 2016
 */
@Singleton
public class StartupClass {

    @Inject
    InjectedClass injectedClass;

    public void init(@Observes ContainerInitialized event, @Parameters List<String> parameters)
    {
        System.out.println(getProperties());
        injectedClass.sayHello();
    }

}
