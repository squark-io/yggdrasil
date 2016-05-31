package io.hakansson.dynamicjar.core.api.util;

import io.hakansson.dynamicjar.core.api.FrameworkProvider;

import java.util.Comparator;

/**
 * Created by Erik Håkansson on 2016-05-31.
 * WirelessCar
 */
public class FrameworkProviderComparator implements Comparator<FrameworkProvider> {
    @Override
    public int compare(FrameworkProvider provider1, FrameworkProvider provider2) {
        for (FrameworkProvider.ProviderDependency providerDependency : provider1.runBefore()) {
            if (providerDependency.name.equals(provider2.getName())) {
                return -1;
            }
        }
        for (FrameworkProvider.ProviderDependency providerDependency : provider1.runAfter()) {
            if (providerDependency.name.equals(provider2.getName())) {
                return 1;
            }
        }
        for (FrameworkProvider.ProviderDependency providerDependency : provider2.runBefore()) {
            if (providerDependency.name.equals(provider1.getName())) {
                return 1;
            }
        }
        for (FrameworkProvider.ProviderDependency providerDependency : provider2.runAfter()) {
            if (providerDependency.name.equals(provider1.getName())) {
                return -1;
            }
        }
        return 0;
    }
}
