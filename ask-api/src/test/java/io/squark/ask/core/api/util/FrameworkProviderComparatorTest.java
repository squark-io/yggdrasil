package io.squark.ask.core.api.util;

import io.squark.ask.core.api.FrameworkProvider;
import io.squark.ask.core.api.exception.AskException;
import io.squark.ask.core.api.model.AskConfiguration;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

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
            public void provide(@Nullable AskConfiguration configuration) throws AskException {

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
            public void provide(@Nullable AskConfiguration configuration) throws AskException {

            }

            @Override
            public String getName() {
                return "frameworkProvider2";
            }

        };

        FrameworkProvider frameworkProvider3 = new FrameworkProvider() {
            @Override
            public void provide(@Nullable AskConfiguration configuration) throws AskException {

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
            public void provide(@Nullable AskConfiguration configuration) throws AskException {

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

        Collections.sort(list, new FrameworkProviderComparator());

        Assert.assertTrue(list.get(0) == frameworkProvider3);
        Assert.assertTrue(list.get(1) == frameworkProvider2);
        Assert.assertTrue(list.get(2) == frameworkProvider1);
        Assert.assertTrue(list.get(3) == frameworkProvider4);
    }

}