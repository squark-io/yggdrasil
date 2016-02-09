package org.dynamicjar.core.model;

import java.util.HashMap;

/**
 * *** AUTOTRADE ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-09.
 * Copyright 2016
 */
public class DependencyTree extends HashMap<String, DependencyNode> {

    private DependencyNode dependencyNode;

    public DependencyTree(DependencyNode dependencyNode) {
        this.dependencyNode = dependencyNode;
    }

    public DependencyNode get(String groupId, String artifactId) {
        return get(groupId + ":" + artifactId);
    }

    public DependencyNode put(DependencyNode dependencyNode) {

        return put(dependencyNode.getGroupId() + ":" + dependencyNode.getArtifactId(), dependencyNode);
    }

    public DependencyNode getDependencyNode() {
        return dependencyNode;
    }
}
