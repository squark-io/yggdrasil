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
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Erik Håkansson on 2016-05-31.
 * WirelessCar
 */
public class FrameworkProviderComparatorTest {

    @Test
    public void testCompare() throws Exception {
        FrameworkProvider frameworkProvider1 = new FrameworkProvider() {
            @Override
            public void provide(@Nullable YggdrasilConfiguration configuration) throws YggdrasilException {

            }

            @Override
            public String getName() {
                return "frameworkProvider1";
            }

            @Override
            public List<ProviderDependency> runAfter() {
                return Collections.singletonList(new ProviderDependency("frameworkProvider2", false));
            }

            @Override
            public List<ProviderDependency> runBefore() {
                return Collections.singletonList(new ProviderDependency("frameworkProvider4", false));
            }
        };

        FrameworkProvider frameworkProvider2 = new FrameworkProvider() {
            @Override
            public void provide(@Nullable YggdrasilConfiguration configuration) throws YggdrasilException {

            }

            @Override
            public String getName() {
                return "frameworkProvider2";
            }

        };

        FrameworkProvider frameworkProvider3 = new FrameworkProvider() {
            @Override
            public void provide(@Nullable YggdrasilConfiguration configuration) throws YggdrasilException {

            }

            @Override
            public String getName() {
                return "frameworkProvider3";
            }

            @Override
            public List<ProviderDependency> runBefore() {
                return Collections.singletonList(new ProviderDependency("frameworkProvider2", false));
            }
        };

        FrameworkProvider frameworkProvider4 = new FrameworkProvider() {
            @Override
            public void provide(@Nullable YggdrasilConfiguration configuration) throws YggdrasilException {

            }

            @Override
            public String getName() {
                return "frameworkProvider4";
            }

            @Override
            public List<ProviderDependency> runAfter() {
                return Collections.singletonList(new ProviderDependency("frameworkProvider2", false));
            }
        };

        List<FrameworkProvider> list = new ArrayList<>();
        list.add(frameworkProvider1);
        list.add(frameworkProvider2);
        list.add(frameworkProvider3);
        list.add(frameworkProvider4);

        Assert.assertTrue(list.get(0) == frameworkProvider1);
        Assert.assertTrue(list.get(1) == frameworkProvider2);
        Assert.assertTrue(list.get(2) == frameworkProvider3);
        Assert.assertTrue(list.get(3) == frameworkProvider4);

        list.sort(new FrameworkProviderComparator());

        Assert.assertTrue(list.get(0) == frameworkProvider3);
        Assert.assertTrue(list.get(1) == frameworkProvider2);
        Assert.assertTrue(list.get(2) == frameworkProvider1);
        Assert.assertTrue(list.get(3) == frameworkProvider4);
    }

}