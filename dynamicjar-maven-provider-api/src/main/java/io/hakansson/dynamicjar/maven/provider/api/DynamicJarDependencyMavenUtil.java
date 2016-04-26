package io.hakansson.dynamicjar.maven.provider.api;

import io.hakansson.dynamicjar.core.api.model.DynamicJarDependency;
import io.hakansson.dynamicjar.core.api.util.Scopes;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.maven.model.Dependency;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-05.
 * Copyright 2016
 */
public class DynamicJarDependencyMavenUtil {

    public static DynamicJarDependency fromDependencyNode(final DependencyNode dependencyNode,
        List<String> exclusions) {
        Artifact artifact = dependencyNode.getArtifact();
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String extension = artifact.getExtension();
        String classifier = artifact.getClassifier();
        String version = artifact.getVersion();
        File file = artifact.getFile();
        Boolean optional = dependencyNode.getDependency().getOptional();
        String scope = dependencyNode.getDependency().getScope();
        Set<DynamicJarDependency> children = new HashSet<>();
        if (CollectionUtils.isNotEmpty(dependencyNode.getChildren())) {
            dependencyNode.getChildren().parallelStream().forEach(child -> {
                children.add(fromDependencyNode(child, exclusions));
            });
        }
        DynamicJarDependency dependency =
            new DynamicJarDependency(groupId, artifactId, extension, classifier, version, file,
                scope, children, Scopes.PROVIDED, optional);
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

    public static DynamicJarDependency fromMavenDependency(final Dependency dependency) {
        return new DynamicJarDependency(dependency.getGroupId(), dependency.getArtifactId(), null,
            dependency.getClassifier(), dependency.getVersion(), null, dependency.getScope(), null,
            Scopes.COMPILE);
    }
}
