package io.hakansson.dynamicjar.core.api.util;

import io.hakansson.dynamicjar.core.api.FrameworkProvider;
import io.hakansson.dynamicjar.core.api.exception.FrameworkProviderException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Erik Håkansson on 2016-05-31.
 * WirelessCar
 */
public class FrameworkProviderUtil {
    public static void validateDependencies(List<FrameworkProvider> providerList) throws FrameworkProviderException {
        for (FrameworkProvider provider : providerList) {
            Set<FrameworkProvider.ProviderDependency> flatDependencies = new HashSet<>();
            flatDependencies.addAll(provider.runBefore());
            flatDependencies.addAll(provider.runAfter());
            for (FrameworkProvider.ProviderDependency dependency : flatDependencies) {
                if (dependency.optional) {
                    continue;
                }
                boolean found = false;
                for (FrameworkProvider dependencyProvider : providerList) {
                    if (dependency.name.equals(dependencyProvider.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new FrameworkProviderException("FrameworkProvider " + provider.getName() + " has a dependency on FrameworkProvider " + dependency.name + " which was not found");
                }
            }
        }
    }
}
