package io.hakansson.dynamicjar.core.api.util;

import io.hakansson.dynamicjar.core.api.FrameworkProvider;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by Erik Håkansson on 2016-05-31.
 * WirelessCar
 */
public class FrameworkProviderComparatorTest {

    @Test
    public void testCompare() throws Exception {
        FrameworkProvider frameworkProvider1 = new FrameworkProvider() {
            @Override
            public void provide(@Nullable DynamicJarConfiguration configuration) throws DynamicJarException {

            }

            @Override
            public String getName() {
                return "frameworkProvider1";
            }

            @Override
            public List<ProviderDependency> runAfter() {
                return Collections.singletonList(new ProviderDependency("frameworkProvider2", false));
            }
        };

        FrameworkProvider frameworkProvider2 = new FrameworkProvider() {
            @Override
            public void provide(@Nullable DynamicJarConfiguration configuration) throws DynamicJarException {

            }

            @Override
            public String getName() {
                return "frameworkProvider2";
            }

        };

        FrameworkProvider frameworkProvider3 = new FrameworkProvider() {
            @Override
            public void provide(@Nullable DynamicJarConfiguration configuration) throws DynamicJarException {

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

        List<FrameworkProvider> list = new ArrayList<>();
        list.add(frameworkProvider1);
        list.add(frameworkProvider2);
        list.add(frameworkProvider3);

        Assert.assertTrue(list.get(0) == frameworkProvider1);
        Assert.assertTrue(list.get(1) == frameworkProvider2);
        Assert.assertTrue(list.get(2) == frameworkProvider3);

        Collections.sort(list, new FrameworkProviderComparator());

        Assert.assertTrue(list.get(0) == frameworkProvider3);
        Assert.assertTrue(list.get(1) == frameworkProvider2);
        Assert.assertTrue(list.get(2) == frameworkProvider1);
    }

}