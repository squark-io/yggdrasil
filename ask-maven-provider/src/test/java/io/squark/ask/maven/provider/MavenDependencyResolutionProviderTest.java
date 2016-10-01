package io.squark.ask.maven.provider;


import io.squark.ask.core.api.exception.DependencyResolutionException;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.internal.impl.DefaultLocalRepositoryProvider;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-06-15.
 * Copyright 2016
 */
public class MavenDependencyResolutionProviderTest {

    @BeforeClass
    public static void setUp() {

    }

    @Test
    public void testNewRepositorySystemSessionWithProxy() throws Exception {
        //Just running through is fine. No asserts necessary here.
        invokeTest(true, true);
        invokeTest(true, false);
        invokeTest(false, false);
    }

    private void invokeTest(boolean setProxy, boolean activeProxy) throws DependencyResolutionException {
        DefaultRepositorySystem repositorySystem = new DefaultRepositorySystem();
        DefaultLocalRepositoryProvider repositoryProvider = new DefaultLocalRepositoryProvider();
        repositoryProvider.addLocalRepositoryManagerFactory(new SimpleLocalRepositoryManagerFactory());
        repositorySystem.setLocalRepositoryProvider(repositoryProvider);
        Settings mavenSettings = new Settings();
        if (setProxy) {
            Proxy proxy = new Proxy();
            proxy.setActive(activeProxy);
            mavenSettings.setProxies(Collections.singletonList(proxy));
        }
        MavenDependencyResolutionProvider provider = new MavenDependencyResolutionProvider() {
            @Override
            protected String getEnv(String key) {
                return "http://mock";
            }
        };
        provider.newRepositorySystemSession(repositorySystem, new LocalRepository("/"), mavenSettings, true);
    }

}