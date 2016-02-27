package org.dynamicjar.test;

import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-27.
 * Copyright 2016
 */
public class StartupClass {

    @Inject
    InjectedClass injectedClass;

    private Logger logger = LoggerFactory.getLogger(StartupClass.class);

    public void main(@Observes ContainerInitialized event)
    {

    }

}
