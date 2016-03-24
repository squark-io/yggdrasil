package io.hakansson.dynamicjar.weld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.util.ArrayList;
import java.util.List;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-13.
 * Copyright 2016
 */
public class ExcludePackageCDIExtension implements Extension {

    private static final List<String> excludedPackages = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(ExcludePackageCDIExtension.class);

    static {
        excludedPackages.add("org.eclipse.aether");
        excludedPackages.add("org.eclipse.sisu");
        excludedPackages.add("org.apache.maven");
    }

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
        for (String excludedPackage : excludedPackages) {
            if (pat.getAnnotatedType().getJavaClass().getName().startsWith(excludedPackage)) {
                logger.debug("Excluding class " + pat.getAnnotatedType().getJavaClass().getName() + " from bean discovery.");
                pat.veto();
            }
        }

    }
}
