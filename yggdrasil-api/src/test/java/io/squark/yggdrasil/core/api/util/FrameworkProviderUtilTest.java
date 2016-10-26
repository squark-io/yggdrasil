/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.squark.yggdrasil.core.api.util;

import io.squark.yggdrasil.core.api.FrameworkProvider;
import io.squark.yggdrasil.core.api.exception.FrameworkProviderException;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FrameworkProviderUtilTest {

    @Test
    public void testValidateDependencies() throws Exception {
        List<FrameworkProvider> frameworkProviders = Arrays.asList(new DummyFrameworkProvider(), new FrameworkProviderWithNonOptionalDep());

        FrameworkProviderUtil.validateDependencies(frameworkProviders);
    }

    @Test
    public void testFailOnDepNotFound() throws Exception {
        try {
            List<FrameworkProvider> frameworkProviders = Collections.singletonList(new FrameworkProviderWithNonExistingDep());

            FrameworkProviderUtil.validateDependencies(frameworkProviders);
        } catch (FrameworkProviderException e) {
            return;
        }
        Assert.fail("Expected FrameworkProviderException");
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