package org.dynamicjar.core.api;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.maven.model.Dependency;
import org.dynamicjar.core.api.model.DynamicJarDependency;
import org.dynamicjar.core.api.util.Scopes;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-05.
 * Copyright 2016
 */
public class DynamicJarDependencyMavenUtil {

    public static DynamicJarDependency fromDependencyNode(final DependencyNode dependencyNode) {
        Artifact artifact = dependencyNode.getArtifact();
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String extension = artifact.getExtension();
        String classifier = artifact.getClassifier();
        String version = artifact.getVersion();
        File file = artifact.getFile();
        String scope = dependencyNode.getDependency().getScope();
        Set<DynamicJarDependency> children = new HashSet<>();
        if (CollectionUtils.isNotEmpty(dependencyNode.getChildren())) {
            dependencyNode.getChildren().parallelStream().forEach(child -> {
                children.add(fromDependencyNode(child));
            });
        }
        return new DynamicJarDependency(groupId, artifactId, extension, classifier, version, file,
            scope, children, Scopes.PROVIDED);
    }

    public static DynamicJarDependency fromMavenDependency(final Dependency dependency) {
        return new DynamicJarDependency(dependency.getGroupId(), dependency.getArtifactId(), null,
            dependency.getClassifier(), dependency.getVersion(), null, dependency.getScope(), null,
            Scopes.COMPILE);
    }
}
