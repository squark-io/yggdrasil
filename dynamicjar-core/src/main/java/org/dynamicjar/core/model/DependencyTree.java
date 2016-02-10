package org.dynamicjar.core.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * *** AUTOTRADE ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-09.
 * Copyright 2016
 */
public class DependencyTree extends HashMap<String, DependencyTreeNode> {

    private DependencyTreeNode dependencyTreeNode;

    public DependencyTree(@NotNull DependencyTreeNode dependencyTreeNode) {
        this.dependencyTreeNode = dependencyTreeNode;
    }

    public DependencyTreeNode get(String groupId, String artifactId, String extension,
        @Nullable String classifier, String version) {
        return get(DependencyTreeNode
            .buildIdentifierString(groupId, artifactId, extension, classifier, version));
    }

    public DependencyTreeNode put(DependencyTreeNode dependencyTreeNode) {

        return put(DependencyTreeNode.buildIdentifierString(dependencyTreeNode),
            dependencyTreeNode);
    }

    public DependencyTreeNode getDependencyTreeNode() {
        return dependencyTreeNode;
    }

    @Override
    public String toString() {
        return "DependencyTree{\n" +
               "\tdependencyTreeNode=" + dependencyTreeNode.buildIdentifierString() +
               ",\n\tnodes=\n" + super.toString() +
               "\n}";
    }
}
