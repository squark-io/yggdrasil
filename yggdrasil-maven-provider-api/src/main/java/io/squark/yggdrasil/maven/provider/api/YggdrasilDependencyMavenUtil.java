package io.squark.yggdrasil.maven.provider.api;

import io.squark.yggdrasil.core.api.model.YggdrasilDependency;
import io.squark.yggdrasil.core.api.util.Scopes;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-03-05.
 * Copyright 2016
 */
public class YggdrasilDependencyMavenUtil {

    public static YggdrasilDependency fromDependencyNode(final DependencyNode dependencyNode,
        List<String> exclusions) {
        Artifact artifact = dependencyNode.getArtifact();
        String groupId = artifact != null ? artifact.getGroupId() : null;
        String artifactId = artifact != null ? artifact.getArtifactId() : null;
        String extension = artifact != null ? artifact.getExtension() : null;
        String classifier = artifact != null ? artifact.getClassifier() : null;
        String version = artifact != null ? artifact.getVersion() : null;
        File file = artifact != null ? artifact.getFile() : null;
        Dependency aetherDependency = dependencyNode.getDependency();
        Boolean optional = aetherDependency != null ? aetherDependency.getOptional() : null;
        String scope = aetherDependency != null ? aetherDependency.getScope() : null;
        Set<YggdrasilDependency> children = new HashSet<>();
        if (CollectionUtils.isNotEmpty(dependencyNode.getChildren())) {
            for (DependencyNode child : dependencyNode.getChildren()) {
                children.add(fromDependencyNode(child, exclusions));
            }
        }
        YggdrasilDependency dependency =
            new YggdrasilDependency(groupId, artifactId, extension, classifier, version, file, scope, children,
                Scopes.PROVIDED, optional);
        if (exclusions != null) {
            for (String exclusion : exclusions) {
                Pattern pattern = Pattern.compile(exclusion);
                Matcher matcher = pattern.matcher(dependency.toShortStringWithoutVersion());
                if (matcher.matches()) {
                    dependency.setExcluded(true);
                }
            }
        }
        return dependency;
    }
}