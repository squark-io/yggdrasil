package io.hakansson.dynamicjar.core.api.util;

import io.hakansson.dynamicjar.core.api.FrameworkProvider;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.exception.FrameworkProviderException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.frameworkprovider.ResteasyFrameworkProvider;
import io.hakansson.dynamicjar.frameworkprovider.WeldFrameworkProvider;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-13.
 * Copyright 2016
 */
public class FrameworkProviderUtilTest {
    @Test
    public void testValidateDependencies() throws Exception {
        List<FrameworkProvider> frameworkProviders = Arrays.asList(new ResteasyFrameworkProvider(), new WeldFrameworkProvider(), new FrameworkProviderWithNonOptionalDep());

        FrameworkProviderUtil.validateDependencies(frameworkProviders);
    }

    @Test(expectedExceptions = FrameworkProviderException.class)
    public void testFailOnDepNotFound() throws Exception {
        List<FrameworkProvider> frameworkProviders = Collections.singletonList(new FrameworkProviderWithNonExistingDep());

        FrameworkProviderUtil.validateDependencies(frameworkProviders);
    }

    public class FrameworkProviderWithNonOptionalDep implements FrameworkProvider {

        @Override
        public void provide(@Nullable DynamicJarConfiguration configuration) throws DynamicJarException {

        }

        @Override
        public List<ProviderDependency> runAfter() {
            return Collections.singletonList(new ProviderDependency(WeldFrameworkProvider.class.getSimpleName(), false));
        }
    }

    public class FrameworkProviderWithNonExistingDep implements FrameworkProvider {

        @Override
        public void provide(@Nullable DynamicJarConfiguration configuration) throws DynamicJarException {

        }

        @Override
        public List<ProviderDependency> runAfter() {
            return Collections.singletonList(new ProviderDependency("Not existing", false));
        }
    }

}