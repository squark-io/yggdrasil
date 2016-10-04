package io.squark.yggdrasil.core.api.util;

import io.squark.yggdrasil.core.api.FrameworkProvider;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.exception.FrameworkProviderException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * yggdrasil
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
        public void provide(@Nullable YggdrasilConfiguration configuration) throws YggdrasilException {

        }
    }

    private class FrameworkProviderWithNonOptionalDep implements FrameworkProvider {

        @Override
        public void provide(@Nullable YggdrasilConfiguration configuration) throws YggdrasilException {

        }

        @Override
        public List<ProviderDependency> runAfter() {
            return Collections.singletonList(new ProviderDependency(DummyFrameworkProvider.class.getSimpleName(), false));
        }
    }

    private class FrameworkProviderWithNonExistingDep implements FrameworkProvider {

        @Override
        public void provide(@Nullable YggdrasilConfiguration configuration) throws YggdrasilException {

        }

        @Override
        public List<ProviderDependency> runAfter() {
            return Collections.singletonList(new ProviderDependency("Not existing", false));
        }
    }

}