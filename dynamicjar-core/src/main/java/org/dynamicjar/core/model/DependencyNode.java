package org.dynamicjar.core.model;

import java.io.File;

/**
 * *** AUTOTRADE ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-09.
 * Copyright 2016
 */
public class DependencyNode {
    private DependencyTree parentTree;
    private String groupId;
    private String artifactId;
    private String version;
    private String classifier;
    private String extension;
    private File file;
    private String scope;
    private DependencyTree childDependencies;

    public DependencyNode(String groupId, String artifactId, String version, String classifier,
        String extension, File file, String scope, DependencyTree parentTree) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.extension = extension;
        this.file = file;
        this.scope = scope;
        this.parentTree = parentTree;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean hasChildDependencies() {
        return childDependencies != null && childDependencies.size() > 0;
    }

    public DependencyTree getChildDependencies() {
        return childDependencies;
    }

    public void addChildDependency(DependencyNode childDependency) {
        if (this.childDependencies == null) {
            this.childDependencies = new DependencyTree(this);
        }
        this.childDependencies.put(childDependency);
    }

    public DependencyTree getParentTree() {
        return parentTree;
    }

    public void setParentTree(DependencyTree parentTree) {
        this.parentTree = parentTree;
    }

    public DependencyNode getParent() {
        return this.parentTree != null ? this.parentTree.getDependencyNode() : null;
    }
}
