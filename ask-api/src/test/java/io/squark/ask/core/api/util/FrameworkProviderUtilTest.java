package io.squark.ask.core.api.util;

import io.squark.ask.core.api.FrameworkProvider;
import io.squark.ask.core.api.exception.AskException;
import io.squark.ask.core.api.exception.FrameworkProviderException;
import io.squark.ask.core.api.model.AskConfiguration;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-06-13.
 * Copyright 2016
 */
public class FrameworkProviderUtilTest {
    @Test
    public void testValidateDependencies() throws Exception {
        List<FrameworkProvider> frameworkProviders = Arrays.asList(new DummyFrameworkProvider(), new FrameworkProviderWithNonOptionalDep());

        FrameworkProviderUtil.validateDependencies(frameworkProviders);
    }

    @Test(expectedExceptions = FrameworkProviderException.class)
    public void testFailOnDepNotFound() throws Exception {
        List<FrameworkProvider> frameworkProviders = Collections.singletonList(new FrameworkProviderWithNonExistingDep());

        FrameworkProviderUtil.validateDependencies(frameworkProviders);
    }

    private class DummyFrameworkProvider implements FrameworkProvider {

        @Override
        public void provide(@Nullable AskConfiguration configuration) throws AskException {

        }
    }

    private class FrameworkProviderWithNonOptionalDep implements FrameworkProvider {

        @Override
        public void provide(@Nullable AskConfiguration configuration) throws AskException {

        }

        @Override
        public List<ProviderDependency> runAfter() {
            return Collections.singletonList(new ProviderDependency(DummyFrameworkProvider.class.getSimpleName(), false));
        }
    }

    private class FrameworkProviderWithNonExistingDep implements FrameworkProvider {

        @Override
        public void provide(@Nullable AskConfiguration configuration) throws AskException {

        }

        @Override
        public List<ProviderDependency> runAfter() {
            return Collections.singletonList(new ProviderDependency("Not existing", false));
        }
    }

}